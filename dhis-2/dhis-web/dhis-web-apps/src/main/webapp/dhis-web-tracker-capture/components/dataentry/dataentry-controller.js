/* global angular, trackerCapture */

trackerCapture.controller('DataEntryController',
        function($rootScope,
                $scope,
				$modal,
				$filter,
                $log,
                $timeout,
                Paginator,
                DateUtils,
                EventUtils,
                orderByFilter,
                SessionStorageService,
                EnrollmentService,
                ProgramStageFactory,
                DHIS2EventFactory,
                OptionSetService,
                ModalService,
                CurrentSelection,
				TrackerRulesExecutionService,
				CustomFormService,
				PeriodService) {
    
    //Data entry form
    $scope.outerForm = {};
    $scope.displayCustomForm = false;
    $scope.currentElement = {};
    $scope.schedulingEnabled = false;
    $scope.eventPeriods = [];
    $scope.currentPeriod = [];
    $scope.filterEvents = true;
    $scope.showEventsAsTables = false;
    //variable is set while looping through the program stages later.
    $scope.stagesCanBeShownAsTable = false;
    $scope.showHelpText = {};
	$scope.hiddenFields = {};
    
    var userProfile = SessionStorageService.get('USER_PROFILE');
    var storedBy = userProfile && userProfile.username ? userProfile.username : '';
    
    var today = DateUtils.getToday();
    $scope.invalidDate = false;
    
    //note
    $scope.note = '';
    
    //event color legend
    $scope.eventColors = [
                            {color: 'alert-success', description: 'completed'},
                            {color: 'alert-info', description: 'executed'},
                            {color: 'alert-warning', description: 'ontime'},
                            {color: 'alert-danger', description: 'overdue'},
                            {color: 'alert-default', description: 'skipped'}
                         ];
    $scope.showEventColors = false;
    
    //listen for rule effect changes
    $scope.$on('ruleeffectsupdated', function(event, args) {
        if($rootScope.ruleeffects[args.event]) {
            //Establish which event was affected:
            var affectedEvent = $scope.currentEvent;
            //In most cases the updated effects apply to the current event. In case the affected event is not the current event, fetch the correct event to affect:
            if(args.event !== affectedEvent.event) {
                angular.forEach($scope.currentStageEvents, function(searchedEvent) {
                    if(searchedEvent.event === args.event) {
                        affectedEvent = searchedEvent;
                    }
                });
            }
            
            angular.forEach($rootScope.ruleeffects[args.event], function(effect) {
                if( effect.dataElement ) {
                    //in the data entry controller we only care about the "hidefield" actions
                    if(effect.action === "HIDEFIELD") {
                        if(effect.dataElement) {
                            if(effect.ineffect && affectedEvent[effect.dataElement.id]) {
                                //If a field is going to be hidden, but contains a value, we need to take action;
                                if(effect.content) {
                                    //TODO: Alerts is going to be replaced with a proper display mecanism.
                                    alert(effect.content);
                                }
                                else {
                                    //TODO: Alerts is going to be replaced with a proper display mecanism.
                                    alert($scope.prStDes[effect.dataElement.id].dataElement.formName + "Was blanked out and hidden by your last action");
                                }

                                //Blank out the value:
                                affectedEvent[effect.dataElement.id] = "";
                                $scope.saveDatavalueForEvent($scope.prStDes[effect.dataElement.id],null,affectedEvent);
                            }

                            $scope.hiddenFields[effect.dataElement.id] = effect.ineffect;
                        }
                        else {
                            $log.warn("ProgramRuleAction " + effect.id + " is of type HIDEFIELD, bot does not have a dataelement defined");
                        }
                    }
                }
            });
        }
    });
    
    //check if field is hidden
    $scope.isHidden = function(id) {
        //In case the field contains a value, we cant hide it. 
        //If we hid a field with a value, it would falsely seem the user was aware that the value was entered in the UI.
        if($scope.currentEvent[id]) {
           return false; 
        }
        else {
            return $scope.hiddenFields[id];
        }
    }; 
    
    $scope.executeRules = function() {
        //If the events is displayed in a table, it is necessary to run the rules for all visible events.
        if($scope.currentStage.displayEventsInTable) {
           angular.forEach($scope.currentStageEvents, function(event) {
               TrackerRulesExecutionService.executeRules($scope.selectedProgramId,event,$scope.eventsByStage,$scope.prStDes,$scope.selectedTei,$scope.selectedEnrollment,false);
           });
        } else {
           TrackerRulesExecutionService.executeRules($scope.selectedProgramId,$scope.currentEvent,$scope.eventsByStage,$scope.prStDes,$scope.selectedTei,$scope.selectedEnrollment,false);
        }
    };
    
    
    //listen for the selected items
    $scope.$on('dashboardWidgets', function() {
        $scope.showDataEntryDiv = false;
        $scope.showEventCreationDiv = false;
        $scope.currentEvent = null;
        $scope.currentStage = null;
        $scope.currentStageEvents = null;
        $scope.totalEvents = 0;
            
        $scope.allowEventCreation = false;
        $scope.repeatableStages = [];        
        $scope.eventsByStage = [];
        $scope.programStages = [];
        $rootScope.ruleeffects = {};
		$scope.prStDes = [];
        
        var selections = CurrentSelection.get();          
        $scope.selectedOrgUnit = SessionStorageService.get('SELECTED_OU');
        $scope.selectedEntity = selections.tei;      
        $scope.selectedProgram = selections.pr;        
        $scope.selectedEnrollment = selections.selectedEnrollment;   
        $scope.optionSets = selections.optionSets;
        
        $scope.stagesById = [];        
        if($scope.selectedOrgUnit && $scope.selectedProgram && $scope.selectedProgram.id && $scope.selectedEntity && $scope.selectedEnrollment && $scope.selectedEnrollment.enrollment){            
            ProgramStageFactory.getByProgram($scope.selectedProgram).then(function(stages){
                $scope.programStages = stages;
                angular.forEach(stages, function(stage){
                    if(stage.openAfterEnrollment){
                        $scope.currentStage = stage;
                    }                   
                    
                    angular.forEach(stage.programStageDataElements, function(prStDe){
                        $scope.prStDes[prStDe.dataElement.id] = prStDe;
                    });
                    
                    $scope.stagesById[stage.id] = stage;
                    $scope.eventsByStage[stage.id] = [];
                    
                    //If one of the stages has less than 7 data elements, allow sorting as table:
                    if(stage.programStageDataElements.length < 7) {
                        $scope.stagesCanBeShownAsTable = true;
                    }
                });
                
                $scope.programStages = orderByFilter($scope.programStages, '-sortOrder').reverse();
                if(!$scope.currentStage){
                    $scope.currentStage = $scope.programStages[0];
                }
                $scope.getEvents();                
            });
        }
    });
    
    $scope.getEvents = function(){
        
        var events = CurrentSelection.getSelectedTeiEvents();
        events = $filter('filter')(events, {program: $scope.selectedProgram.id});
        
        if(angular.isObject(events)){
            angular.forEach(events, function(dhis2Event){                    
                if(dhis2Event.enrollment === $scope.selectedEnrollment.enrollment && dhis2Event.orgUnit){
                    if(dhis2Event.notes){
                        dhis2Event.notes = orderByFilter(dhis2Event.notes, '-storedDate');
                        angular.forEach(dhis2Event.notes, function(note){
                            note.storedDate = DateUtils.formatToHrsMins(note.storedDate);
                        });
                    }

                    var eventStage = $scope.stagesById[dhis2Event.programStage];
                    if(angular.isObject(eventStage)){
                        dhis2Event.name = eventStage.name; 
                        dhis2Event.reportDateDescription = eventStage.reportDateDescription;
                        dhis2Event.dueDate = DateUtils.formatFromApiToUser(dhis2Event.dueDate);
                        dhis2Event.sortingDate = dhis2Event.dueDate;

                        if(dhis2Event.eventDate){
                            dhis2Event.eventDate = DateUtils.formatFromApiToUser(dhis2Event.eventDate);
                            dhis2Event.sortingDate = dhis2Event.eventDate;
                            dhis2Event.editingNotAllowed = setEventEditing(dhis2Event, eventStage);
                        }                       

                        dhis2Event.statusColor = EventUtils.getEventStatusColor(dhis2Event);
                        dhis2Event = EventUtils.processEvent(dhis2Event, eventStage, $scope.optionSets, $scope.prStDes);
                        $scope.eventsByStage[dhis2Event.programStage].push(dhis2Event);

                        if($scope.currentStage && $scope.currentStage.id === dhis2Event.programStage){
                            $scope.currentEvent = dhis2Event; 
                        }
                    }
                }
            });
            
            sortEventsByStage(null);
            $scope.showDataEntry($scope.currentEvent, true);            
        }
    };
    
    var setEventEditing = function(dhis2Event, stage){
        return dhis2Event.editingNotAllowed = dhis2Event.orgUnit !== $scope.selectedOrgUnit.id || (stage.blockEntryForm && dhis2Event.status === 'COMPLETED');
    };
    
    $scope.enableRescheduling = function(){
        $scope.schedulingEnabled = !$scope.schedulingEnabled;
    };
    
    $scope.stageCanBeShownAsTable = function(stage) {
        if(stage.programStageDataElements && stage.programStageDataElements.length < 7) {
            return true;
        }
        return false;
    };
    
    $scope.toggleEventsTableDisplay = function() {
        $scope.showEventsAsTables = !$scope.showEventsAsTables;
        angular.forEach($scope.programStages, function(stage){
            if(stage.programStageDataElements.length < 7) {
                stage.displayEventsInTable = $scope.showEventsAsTables;
                if($scope.currentStage === stage) {
                    $scope.getDataEntryForm();
                }
            }
        });
    };
    
    $scope.stageNeedsEvent = function(stage){
        
        //In case the event is a table, we sould always allow adding more events(rows)
        if(stage.displayEventsInTable) {
            return true;
        }
        
        if($scope.eventsByStage[stage.id].length < 1){
            return true;
        }

        if(stage.repeatable){
            for(var j=0; j<$scope.eventsByStage[stage.id].length; j++){
                if(!$scope.eventsByStage[stage.id][j].eventDate && $scope.eventsByStage[stage.id][j].status !== 'SKIPPED'){
                    return false;
                }
            }
            return true;            
        }        
        return false;        
    };
    
    $scope.showCreateEvent = function(stage){
        
        var dummyEvent = EventUtils.createDummyEvent($scope.eventsByStage[stage.id], $scope.selectedEntity, $scope.selectedProgram, stage, $scope.selectedOrgUnit, $scope.selectedEnrollment);
        
        var modalInstance = $modal.open({
            templateUrl: 'components/dataentry/new-event.html',
            controller: 'EventCreationController',
            resolve: {
                stagesById: function(){
                    return $scope.stagesById;
                },
                dummyEvent: function(){
                    return dummyEvent;
                },
                eventPeriods: function(){
                    return $scope.eventPeriods;
                },
                autoCreate: function() {
                    //In case the programstage is a table, autocreate
                    return stage.displayEventsInTable;
                }
            }
        });

        modalInstance.result.then(function (ev) {
            if(angular.isObject(ev)){
                var newEvent = ev;
                newEvent.orgUnitName = dummyEvent.orgUnitName;
                newEvent.name = dummyEvent.name;
                newEvent.reportDateDescription = dummyEvent.reportDateDescription;
                newEvent.sortingDate = ev.eventDate ? ev.eventDate : ev.dueDate,
                newEvent.statusColor = EventUtils.getEventStatusColor(ev);
                newEvent.eventDate = DateUtils.formatFromApiToUser(ev.eventDate);
                newEvent.dueDate =  DateUtils.formatFromApiToUser(ev.dueDate);
                newEvent.enrollmentStatus = dummyEvent.enrollmentStatus;
                
                if(dummyEvent.coordinate){
                    newEvent.coordinate = {};
                }
                
                //Have to make sure the event is preprocessed - this does not happen unless "Dashboardwidgets" is invoked.
                newEvent = EventUtils.processEvent(newEvent, stage, $scope.optionSets, $scope.prStDes);
                
                
                $scope.eventsByStage[newEvent.programStage].push(newEvent);
                $scope.currentEvent = newEvent;
                sortEventsByStage('ADD');

                $scope.currentEvent = null;
                $scope.showDataEntry(newEvent, false);
            }            
        }, function () {
        });
    };    
       
    $scope.showDataEntry = function(event, rightAfterEnrollment){        
        if(event){
            
            Paginator.setItemCount( $scope.eventsByStage[event.programStage].length );
            Paginator.setPage( $scope.eventsByStage[event.programStage].indexOf( event ) );
            Paginator.setPageCount( Paginator.getItemCount() );
            Paginator.setPageSize( 1 );
            Paginator.setToolBarDisplay( 5 );
        
            if($scope.currentEvent && !rightAfterEnrollment && $scope.currentEvent.event === event.event){
                //clicked on the same stage, do toggling
                $scope.currentEvent = null;
                $scope.currentElement = {id: '', saved: false};
                $scope.showDataEntryDiv = !$scope.showDataEntryDiv;      
            }
            else{
                $scope.currentElement = {};
                $scope.currentEvent = event;
                $scope.showDataEntryDiv = true;
                $scope.showEventCreationDiv = false;

                if($scope.currentEvent.notes){
                    angular.forEach($scope.currentEvent.notes, function(note){
                        note.storedDate = DateUtils.formatToHrsMins(note.storedDate);
                    });

                    if($scope.currentEvent.notes.length > 0 ){
                        $scope.currentEvent.notes = orderByFilter($scope.currentEvent.notes, '-storedDate');
                    }
                }
                
                $scope.getDataEntryForm();
            }               
        }
    }; 
    
    $scope.switchDataEntryForm = function(){
        $scope.displayCustomForm = !$scope.displayCustomForm;
    };
    
    $scope.getDataEntryForm = function(){
        
        $scope.currentStage = $scope.stagesById[$scope.currentEvent.programStage];
        $scope.currentStageEvents = $scope.eventsByStage[$scope.currentEvent.programStage];
        
        angular.forEach($scope.currentStage.programStageSections, function(section){
            section.open = true;
        });

        $scope.customForm = CustomFormService.getForProgramStage($scope.currentStage, $scope.prStDes);
        $scope.displayCustomForm = "default";
        if($scope.customForm){
            $scope.displayCustomForm = "custom";
        }
        else if($scope.currentStage.displayEventsInTable) {
            $scope.displayCustomForm = "table";
        }
        
        $scope.currentEventOriginal = angular.copy($scope.currentEvent);

        $scope.currentStageEventsOriginal = angular.copy($scope.currentStageEvents);
        
        var period = {event: $scope.currentEvent.event, stage: $scope.currentEvent.programStage, name: $scope.currentEvent.sortingDate};
        $scope.currentPeriod[$scope.currentEvent.programStage] = period;

        //Execute rules for the first time, to make the initial page appear correctly.
        //Subsequent calls will be made from the "saveDataValue" function.
        $scope.executeRules();
    };
    
    function updateCurrentEventInStage(){
        
        var index = -1;
        for(var i=0; i<$scope.eventsByStage[$scope.currentEvent.programStage].length && index === -1; i++){
            if($scope.eventsByStage[$scope.currentEvent.programStage][i].event === $scope.currentEvent.event){
                index = i;
            }
        }
        if(index !== -1){
            $scope.eventsByStage[$scope.currentEvent.programStage].splice(index,1,$scope.currentEvent);
        }
        
    };

    $scope.saveDatavalue = function(prStDe,field){
        $scope.saveDatavalueForEvent(prStDe,field,$scope.currentEvent);
    };

    $scope.saveDatavalueForEvent = function(prStDe,field,eventToSave){
        //Blank out the input-saved class on the last saved due date:
        $scope.eventDateSaved = false;
        
        //console.log('the field:  ', field);
        $scope.currentElement = {};
        
        //check for input validity
        //$scope.outerForm.submitted = true;            
        $scope.updateSuccess = false;
        if( field && field.$invalid ){ 
            //console.log('form is invalid...');
            $scope.currentElement = {id: prStDe.dataElement.id, saved: false};
            return false;
        }
        
        //input is valid
        var value = eventToSave[prStDe.dataElement.id];
        
        var oldValue = null;
        angular.forEach($scope.currentStageEventsOriginal, function (eventOriginal) {
            if(eventOriginal.event === eventToSave.event) {
               oldValue = eventOriginal[prStDe.dataElement.id];
            }
        });
        
        if(oldValue !== value){
            if(value){
                if(prStDe.dataElement.type === 'date'){                    
                    value = DateUtils.formatFromUserToApi(value);
                }
                if(prStDe.dataElement.optionSetValue){                    
                    if(prStDe.dataElement.optionSet && $scope.optionSets[prStDe.dataElement.optionSet.id] &&  $scope.optionSets[prStDe.dataElement.optionSet.id].options ) {
                        value = OptionSetService.getCode($scope.optionSets[prStDe.dataElement.optionSet.id].options, value);
                    }                    
                }
            }

            $scope.updateSuccess = false;

            $scope.currentElement = {id: prStDe.dataElement.id, event:eventToSave.event, saved: false};

            var ev = {  event: eventToSave.event,
                        orgUnit: eventToSave.orgUnit,
                        program: eventToSave.program,
                        programStage: eventToSave.programStage,
                        status: eventToSave.status,
                        trackedEntityInstance: eventToSave.trackedEntityInstance,
                        dataValues: [
                                        {
                                            dataElement: prStDe.dataElement.id, 
                                            value: value, 
                                            providedElsewhere: eventToSave.providedElsewhere[prStDe.dataElement.id] ? true : false
                                        }
                                    ]
                     };
            DHIS2EventFactory.updateForSingleValue(ev).then(function(response){                
                updateCurrentEventInStage();
                sortEventsByStage('UPDATE');
                
                $scope.currentElement.saved = true;
                
                $scope.currentEventOriginal = angular.copy($scope.currentEvent);
                
                $scope.currentStageEventsOriginal = angular.copy($scope.currentStageEvents);

                //Run rules on updated data:
		$scope.executeRules();
            });
            
        }
    };
    
    $scope.saveDatavalueLocation = function(prStDe){
                
        $scope.updateSuccess = false;
        
        if(!angular.isUndefined($scope.currentEvent.providedElsewhere[prStDe.dataElement.id])){

            //currentEvent.providedElsewhere[prStDe.dataElement.id];
            var value = $scope.currentEvent[prStDe.dataElement.id];
            var ev = {  event: $scope.currentEvent.event,
                        orgUnit: $scope.currentEvent.orgUnit,
                        program: $scope.currentEvent.program,
                        programStage: $scope.currentEvent.programStage,
                        status: $scope.currentEvent.status,
                        trackedEntityInstance: $scope.currentEvent.trackedEntityInstance,
                        dataValues: [
                                        {
                                            dataElement: prStDe.dataElement.id, 
                                            value: value, 
                                            providedElsewhere: $scope.currentEvent.providedElsewhere[prStDe.dataElement.id] ? true : false
                                        }
                                    ]
                     };
            DHIS2EventFactory.updateForSingleValue(ev).then(function(response){                
                updateCurrentEventInStage();
                sortEventsByStage('UPDATE');
                
                $scope.updateSuccess = true;
            });
        }
    };
    
    $scope.saveEventDate = function () {
        $scope.saveEventDateForEvent($scope.currentEvent);
    };
    
    $scope.saveEventDateForEvent = function(eventToSave){
        $scope.eventDateSaved = false;
        if(eventToSave.eventDate === ''){            
            $scope.invalidDate = eventToSave.event;
            return false;
        }
        
        var rawDate = angular.copy(eventToSave.eventDate);
        var convertedDate = DateUtils.format(eventToSave.eventDate);
        
        if(rawDate !== convertedDate){
            $scope.invalidDate = true;
            return false;
        }
        
        var e = {event: eventToSave.event,
             enrollment: eventToSave.enrollment,
             dueDate: DateUtils.formatFromUserToApi(eventToSave.dueDate),
             status: eventToSave.status === 'SCHEDULE' ? 'ACTIVE' : eventToSave.status,
             program: eventToSave.program,
             programStage: eventToSave.programStage,
             orgUnit: eventToSave.dataValues && eventToSave.length > 0 ? eventToSave.orgUnit : $scope.selectedOrgUnit.id,
             eventDate: DateUtils.formatFromUserToApi(eventToSave.eventDate),
             trackedEntityInstance: eventToSave.trackedEntityInstance
            };

        DHIS2EventFactory.updateForEventDate(e).then(function(data){
            eventToSave.sortingDate = eventToSave.eventDate;
            $scope.invalidDate = false;
            $scope.eventDateSaved = eventToSave.event;
            eventToSave.statusColor = EventUtils.getEventStatusColor(eventToSave);
            sortEventsByStage();
            updateCurrentEventInStage();
            sortEventsByStage('UPDATE');
        });
    };
    
    $scope.saveDueDate = function(){
        
        $scope.dueDateSaved = false;

        if($scope.currentEvent.dueDate === ''){
            $scope.invalidDate = true;
            return false;
        }
        
        var rawDate = angular.copy($scope.currentEvent.dueDate);
        var convertedDate = DateUtils.format($scope.currentEvent.dueDate);           

        if(rawDate !== convertedDate){
            $scope.invalidDate = true;
            return false;
        } 
        
        var e = {event: $scope.currentEvent.event,
             enrollment: $scope.currentEvent.enrollment,
             dueDate: DateUtils.formatFromUserToApi($scope.currentEvent.dueDate),
             status: $scope.currentEvent.status,
             program: $scope.currentEvent.program,
             programStage: $scope.currentEvent.programStage,
             orgUnit: $scope.selectedOrgUnit.id,
             trackedEntityInstance: $scope.currentEvent.trackedEntityInstance
            };
        
        if($scope.currentStage.periodType){
            e.eventDate = e.dueDate;
        }
        
        if($scope.currentEvent.coordinate){
            e.coordinate = $scope.currentEvent.coordinate;
        }
            
        DHIS2EventFactory.update(e).then(function(data){            
            $scope.invalidDate = false;
            $scope.dueDateSaved = true;
            
            if(e.eventDate && !$scope.currentEvent.eventDate && $scope.currentStage.periodType){
                $scope.currentEvent.eventDate = $scope.currentEvent.dueDate;
            }
            
            $scope.currentEvent.sortingDate = $scope.currentEvent.dueDate;            
            $scope.currentEvent.statusColor = EventUtils.getEventStatusColor($scope.currentEvent);            
            $scope.schedulingEnabled = !$scope.schedulingEnabled;
            
            updateCurrentEventInStage();
            sortEventsByStage('UPDATE');
        });
                      
    };
    
    $scope.saveCoordinate = function(type){
        
        if(type === 'LAT' || type === 'LATLNG' ){
            $scope.latitudeSaved = false;
        }
        if(type === 'LAT' || type === 'LATLNG'){
            $scope.longitudeSaved = false;
        }
        
        if( (type === 'LAT' || type === 'LATLNG') && $scope.outerForm.latitude.$invalid  || 
            (type === 'LNG' || type === 'LATLNG') && $scope.outerForm.longitude.$invalid ){//invalid coordinate            
            return;            
        }
        
        if( (type === 'LAT' || type === 'LATLNG') && $scope.currentEvent.coordinate.latitude === $scope.currentEventOriginal.coordinate.latitude  || 
            (type === 'LNG' || type === 'LATLNG') && $scope.currentEvent.coordinate.longitude === $scope.currentEventOriginal.coordinate.longitude){//no change            
            return;            
        }
        
        //valid coordinate(s), proceed with the saving
        var dhis2Event = EventUtils.reconstruct($scope.currentEvent, $scope.currentStage, $scope.optionSets);
        
        DHIS2EventFactory.update(dhis2Event).then(function(response){            
            $scope.currentEventOriginal = angular.copy($scope.currentEvent);
            $scope.currentStageEventsOriginal = angular.copy($scope.currentStageEvents);

            if(type === 'LAT' || type === 'LATLNG' ){
                $scope.latitudeSaved = true;
            }
            if(type === 'LAT' || type === 'LATLNG'){
                $scope.longitudeSaved = true;
            }
            
            updateCurrentEventInStage();
            sortEventsByStage('UPDATE');
        });
    };
    
    $scope.addNote = function(){
        if(!angular.isUndefined($scope.note) && $scope.note !== ""){
            var newNote = {value: $scope.note};

            if(angular.isUndefined( $scope.currentEvent.notes) ){
                $scope.currentEvent.notes = [{value: $scope.note, storedDate: today, storedBy: storedBy}];
            }
            else{
                $scope.currentEvent.notes.splice(0,0,{value: $scope.note, storedDate: today, storedBy: storedBy});
            }

            var e = {event: $scope.currentEvent.event,
                     program: $scope.currentEvent.program,
                     programStage: $scope.currentEvent.programStage,
                     orgUnit: $scope.currentEvent.orgUnit,
                     trackedEntityInstance: $scope.currentEvent.trackedEntityInstance,
                     notes: [newNote]
                    };

            DHIS2EventFactory.updateForNote(e).then(function(data){
                $scope.note = ''; 
                
                updateCurrentEventInStage();
                sortEventsByStage('UPDATE');
            });
        }        
    };    
    
    $scope.clearNote = function(){
         $scope.note = '';           
    };
    
    $scope.getInputDueDateClass = function(event) {
        if(event.event === $scope.eventDateSaved) {
            return 'input-success';
        }
        else {
            return '';
        }
            
    };
    
    /*$scope.getInputNotifcationClass = function(id, custom, event){
        if(!event) {
            event = $scope.currentEvent;
        }
        if($scope.currentElement.id && $scope.currentElement.event){
            if($scope.currentElement.saved && ($scope.currentElement.id === id && $scope.currentElement.event === event.event)){
                
                if(custom){
                    return 'input-success';
                }
                return 'form-control input-success';
            }            
            if(!$scope.currentElement.saved && ($scope.currentElement.id === id && $scope.currentElement.event === event.event)){
                if(custom){
                    return 'input-error';
                }
                return 'form-control input-error';
            }            
        }  
        if(custom){
            return '';
        }
        return 'form-control';
    };*/
            
    //Infinite Scroll
    $scope.infiniteScroll = {};
    $scope.infiniteScroll.optionsToAdd = 20;
    $scope.infiniteScroll.currentOptions = 20;
    
    $scope.resetInfScroll = function() {
        $scope.infiniteScroll.currentOptions = $scope.infiniteScroll.optionsToAdd;
    };
  
    $scope.addMoreOptions = function(){
        $scope.infiniteScroll.currentOptions += $scope.infiniteScroll.optionsToAdd;
    };        
    
    $scope.getInputNotifcationClass = function(id, custom, event){
        if(!event) {
            event = $scope.currentEvent;
        }
        if($scope.currentElement.id && 
                $scope.currentElement.event && 
                $scope.currentElement.id === id &&
                $scope.currentElement.event === event.event){            
            return $scope.currentElement.saved ? 'input-success; ' : 'input-error; ';                      
        }  
        
        return '';
    };
    
    var completeEnrollment = function(){
        var modalOptions = {
            closeButtonText: 'cancel',
            actionButtonText: 'complete',
            headerText: 'complete_enrollment',
            bodyText: 'would_you_like_to_complete_enrollment'
        };

        ModalService.showModal({}, modalOptions).then(function(result){            
            EnrollmentService.complete($scope.selectedEnrollment).then(function(data){                
                $scope.selectedEnrollment.status = 'COMPLETED';            
            });
        });
    };
    
    $scope.completeIncompleteEvent = function(){
        var modalOptions;
        var dhis2Event = EventUtils.reconstruct($scope.currentEvent, $scope.currentStage, $scope.optionSets);        
        if($scope.currentEvent.status === 'COMPLETED'){//activiate event
            modalOptions = {
                closeButtonText: 'cancel',
                actionButtonText: 'incomplete',
                headerText: 'incomplete',
                bodyText: 'are_you_sure_to_incomplete_event'
            };
            dhis2Event.status = 'ACTIVE';        
        }
        else{//complete event
            modalOptions = {
                closeButtonText: 'cancel',
                actionButtonText: 'complete',
                headerText: 'complete',
                bodyText: 'are_you_sure_to_complete_event'
            };
            dhis2Event.status = 'COMPLETED';
        }        

        ModalService.showModal({}, modalOptions).then(function(result){
            
            DHIS2EventFactory.update(dhis2Event).then(function(data){
                
                if($scope.currentEvent.status === 'COMPLETED'){//activiate event                    
                    $scope.currentEvent.status = 'ACTIVE'; 
                }
                else{//complete event                    
                    $scope.currentEvent.status = 'COMPLETED';
                }
                
                setStatusColor();
                updateCurrentEventInStage();
                sortEventsByStage('UPDATE');
                
                setEventEditing($scope.currentEvent, $scope.currentStage);
                
                if($scope.currentEvent.status === 'COMPLETED'){
                    
                    if($scope.currentStage.remindCompleted){
                        completeEnrollment($scope.currentStage);
                    }
                    else{
                        if($scope.currentStage.allowGenerateNextVisit){
                            $scope.showCreateEvent($scope.currentStage);
                        }
                    }
                }                
            });
        });
    };
    
    $scope.skipUnskipEvent = function(){
        var modalOptions;
        var dhis2Event = EventUtils.reconstruct($scope.currentEvent, $scope.currentStage, $scope.optionSets);   

        if($scope.currentEvent.status === 'SKIPPED'){//unskip event
            modalOptions = {
                closeButtonText: 'cancel',
                actionButtonText: 'unskip',
                headerText: 'unskip',
                bodyText: 'are_you_sure_to_unskip_event'
            };
            dhis2Event.status = 'ACTIVE';        
        }
        else{//skip event
            modalOptions = {
                closeButtonText: 'cancel',
                actionButtonText: 'skip',
                headerText: 'skip',
                bodyText: 'are_you_sure_to_skip_event'
            };
            dhis2Event.status = 'SKIPPED';
        }        

        ModalService.showModal({}, modalOptions).then(function(result){
            
            DHIS2EventFactory.update(dhis2Event).then(function(data){
                
                if($scope.currentEvent.status === 'SKIPPED'){//activiate event                    
                    $scope.currentEvent.status = 'SCHEDULE'; 
                }
                else{//complete event                    
                    $scope.currentEvent.status = 'SKIPPED';
                }
                
                setStatusColor();
                setEventEditing($scope.currentEvent, $scope.currentStage);
                updateCurrentEventInStage();
                sortEventsByStage('UPDATE');
            });
        });
    };
    
    var setStatusColor = function(){
        var statusColor = EventUtils.getEventStatusColor($scope.currentEvent);  
        var continueLoop = true;
        for(var i=0; i< $scope.eventsByStage[$scope.currentEvent.programStage].length && continueLoop; i++){
            if($scope.eventsByStage[$scope.currentEvent.programStage][i].event === $scope.currentEvent.event ){
                $scope.eventsByStage[$scope.currentEvent.programStage][i].statusColor = statusColor;
                $scope.currentEvent.statusColor = statusColor;
                continueLoop = false;
            }
        }
    };
    
    $scope.validateEvent = function(){};    
    
    $scope.deleteEvent = function(){
        
        var modalOptions = {
            closeButtonText: 'cancel',
            actionButtonText: 'delete',
            headerText: 'delete',
            bodyText: 'are_you_sure_to_delete_event'
        };

        ModalService.showModal({}, modalOptions).then(function(result){
            
            DHIS2EventFactory.delete($scope.currentEvent).then(function(data){
                
                var continueLoop = true, index = -1;
                for(var i=0; i< $scope.eventsByStage[$scope.currentEvent.programStage].length && continueLoop; i++){
                    if($scope.eventsByStage[$scope.currentEvent.programStage][i].event === $scope.currentEvent.event ){
                        $scope.eventsByStage[$scope.currentEvent.programStage][i] = $scope.currentEvent;
                        continueLoop = false;
                        index = i;
                    }
                }
                $scope.eventsByStage[$scope.currentEvent.programStage].splice(index,1);                
                sortEventsByStage('REMOVE');
                $scope.currentEvent = null;
            });
        });
    };
    
    $scope.toggleLegend = function(){
        $scope.showEventColors = !$scope.showEventColors;
    };
    
    $scope.getEventStyle = function(ev){
        var style = EventUtils.getEventStatusColor(ev);
        
        if($scope.currentEvent && $scope.currentEvent.event === ev.event){
            style = style + ' ' + 'current-stage';
        }       
        return style;
    };
    
    $scope.getColumnWidth = function(weight){        
        var width = weight <= 1 ? 1 : weight;
        width = (width/$scope.totalEvents)*100;
        return "width: " + width + '%';
    };
    
    $scope.sortEventsByDate = function(dhis2Event){
        var d = dhis2Event.sortingDate;         
        return DateUtils.getDate(d);                
    };
    
    var sortEventsByStage = function(operation){
        
        $scope.eventFilteringRequired = false;
        
        for(var key in $scope.eventsByStage){
            
            var stage = $scope.stagesById[key];
            
            if($scope.eventsByStage.hasOwnProperty(key) && stage){                
                
                var sortedEvents = $filter('orderBy')($scope.eventsByStage[key], function(event) {
                    return DateUtils.getDate(event.sortingDate);
                }, true);
                
                $scope.eventsByStage[key] = sortedEvents;
                
                var periods = PeriodService.getPeriods(sortedEvents, stage, $scope.selectedEnrollment).occupiedPeriods;
                
                $scope.eventPeriods[key] = periods;
                $scope.currentPeriod[key] = periods.length > 0 ? periods[0] : null;  
                $scope.eventFilteringRequired = $scope.eventFilteringRequired ? $scope.eventFilteringRequired : periods.length > 1;                
            }
        }
        
        if(operation !== null){
            
            var evs = CurrentSelection.getSelectedTeiEvents();
            
            if( operation ===  'ADD' ){
                var ev = EventUtils.reconstruct($scope.currentEvent, $scope.currentStage, $scope.optionSets);
                ev.enrollment = $scope.currentEvent.enrollment;
                ev.visited = $scope.currentEvent.visited;
                evs.push(ev);
            }   
            if( operation === 'UPDATE' ){                
                var ev = EventUtils.reconstruct($scope.currentEvent, $scope.currentStage, $scope.optionSets);
                ev.enrollment = $scope.currentEvent.enrollment;
                ev.visited = $scope.currentEvent.visited;
                var index = -1;
                for(var i=0; i<evs.length && index === -1; i++){
                    if(evs[i].event === $scope.currentEvent.event){
                        index = i;
                    }
                }
                if(index !== -1){
                    evs[index] = ev;
                }            
            }
            if( operation === 'REMOVE' ){
                var index = -1;
                for(var i=0; i<evs.length && index === -1; i++){
                    if(evs[i].event === $scope.currentEvent.event){
                        index = i;
                    }
                }
                if(index !== -1){
                    evs.splice(index,1);
                }                        
            }
            
            CurrentSelection.setSelectedTeiEvents( evs );
            
            $timeout(function() { 
                $rootScope.$broadcast('tei-report-widget', {});            
            }, 100);
        }        
    };
    
    $scope.showLastEventInStage = function(stageId){
        var ev = $scope.eventsByStage[stageId][$scope.eventsByStage[stageId].length-1];
        $scope.showDataEntryForEvent(ev);
    };
    
    $scope.showDataEntryForEvent = function(event){
        
        var period = {event: event.event, stage: event.programStage, name: event.sortingDate};
        $scope.currentPeriod[event.programStage] = period;
        
        var event = null;
        for(var i=0; i<$scope.eventsByStage[period.stage].length; i++){
            if($scope.eventsByStage[period.stage][i].event === period.event){
                event = $scope.eventsByStage[period.stage][i];
                break;
            }
        }
        
        if(event){
            $scope.showDataEntry(event, false);
        }
        
    };
    
    $scope.showMap = function(event){
        var modalInstance = $modal.open({
            templateUrl: '../dhis-web-commons/angular-forms/map.html',
            controller: 'MapController',
            windowClass: 'modal-full-window',
            resolve: {
                location: function () {
                    return {lat: event.coordinate.latitude, lng: event.coordinate.longitude};
                }
            }
        });

        modalInstance.result.then(function (location) {
            if(angular.isObject(location)){
                event.coordinate.latitude = location.lat;
                event.coordinate.longitude = location.lng;                
                $scope.saveCoordinate('LATLNG');
            }
        }, function () {
        });
    };
    
    $scope.interacted = function(field) {
        var status = false;
        if(field){            
            status = $scope.outerForm.submitted || field.$dirty;
        }        
        return status;        
    };
    
})

.controller('EventCreationController', 
    function($scope, 
            $modalInstance, 
            DateUtils,
            DHIS2EventFactory,
            DialogService,
            stagesById,
            dummyEvent,
            eventPeriods,
            autoCreate){
    $scope.stagesById = stagesById;
    $scope.programStageId = dummyEvent.programStage;
    $scope.eventPeriods = eventPeriods;
    $scope.selectedStage =  $scope.stagesById[dummyEvent.programStage];
    
    $scope.dhis2Event = {eventDate: '', dueDate: dummyEvent.dueDate, reportDateDescription: dummyEvent.reportDateDescription, name: dummyEvent.name, invalid: true};
    
    if($scope.selectedStage.periodType){
        $scope.dhis2Event.eventDate = dummyEvent.dueDate;
        $scope.dhis2Event.periodName = dummyEvent.periodName;
        $scope.dhis2Event.periods = dummyEvent.periods;
        $scope.dhis2Event.selectedPeriod = dummyEvent.periods[0];
    }
    
    $scope.dueDateInvalid = false;
    $scope.eventDateInvalid = false;
    
    //watch for changes in due/event-date
    $scope.$watchCollection('[dhis2Event.dueDate, dhis2Event.eventDate]', function() {        
        if(angular.isObject($scope.dhis2Event)){
            if(!$scope.dhis2Event.dueDate){
                $scope.dueDateInvalid = true;
                return;
            }
            
            if($scope.dhis2Event.dueDate){
                var rDueDate = $scope.dhis2Event.dueDate;
                var cDueDate = DateUtils.format($scope.dhis2Event.dueDate);                
                $scope.dueDateInvalid = rDueDate !== cDueDate;
            }
            
            if($scope.dhis2Event.eventDate){
                var rEventDate = $scope.dhis2Event.eventDate;
                var cEventDate = DateUtils.format($scope.dhis2Event.eventDate);
                $scope.eventDateInvalid = rEventDate !== cEventDate;
            }
        }
    });
    
    $scope.save = function () {
        //check for form validity
        if($scope.dueDateInvalid || $scope.eventDateInvalid){
            return false;
        }
        
        if($scope.selectedStage.periodType){
            $scope.dhis2Event.eventDate = $scope.dhis2Event.selectedPeriod.endDate;
            $scope.dhis2Event.dueDate = $scope.dhis2Event.selectedPeriod.endDate;
        }        
        
        var eventDate = DateUtils.formatFromUserToApi($scope.dhis2Event.eventDate);
        var dueDate = DateUtils.formatFromUserToApi($scope.dhis2Event.dueDate);
        var newEvents = {events: []};
        var newEvent = {
                trackedEntityInstance: dummyEvent.trackedEntityInstance,
                program: dummyEvent.program,
                programStage: dummyEvent.programStage,
                enrollment: dummyEvent.enrollment,
                orgUnit: dummyEvent.orgUnit,                        
                dueDate: dueDate,
                eventDate: eventDate,
                notes: [],
                dataValues: [],
                status: 'ACTIVE'
            };            
        
        newEvent.status = newEvent.eventDate ? 'ACTIVE' : 'SCHEDULE';
        
        newEvents.events.push(newEvent);
        DHIS2EventFactory.create(newEvents).then(function(data){
            if (data.importSummaries[0].status === 'ERROR') {
                var dialogOptions = {
                    headerText: 'event_creation_error',
                    bodyText: data.importSummaries[0].description
                };

                DialogService.showDialog({}, dialogOptions);
            }
            else {
                newEvent.event = data.importSummaries[0].reference;                
                $modalInstance.close(newEvent);
            }
        });
        
        
    };
    
    //If the caller wants to create right away, go ahead and save.
    if(autoCreate) {
        $scope.save();
    };
    
    $scope.cancel = function(){
        $modalInstance.close();
    };      
});
