// Current offset, next or previous corresponding to increasing or decreasing
// value with one
var currentPeriodOffset = 0;

// Period type object
var periodTypeFactory = new PeriodType();

// The current selected report type name
var currentReportTypeName = '';

// The current selected period type name
var currentPeriodTypeName = '';

// The current selected orgunit name
var currentOrgunitName = '';

// Functions
function organisationUnitSelected( orgUnits, orgUnitNames )
{
	currentOrgunitName = orgUnitNames[0];
	getExportReportsByGroup( currentOrgunitName );	
}

selection.setListenerFunction( organisationUnitSelected );

function getExportReportsByGroup( selectedOrgUnitName ) {

	if ( selectedOrgUnitName )
	{
		setInnerHTML( "selectedOrganisationUnit", selectedOrgUnitName );
		
		jQuery.postJSON( 'getExportReportsByGroup.action',
		{
			group: getFieldValue( 'group' )
		},
		function ( json )
		{
			jQuery('#exportReport').empty();
			jQuery.each( json.exportReports, function(i, item){
				addOptionById( 'exportReport', item.id + '_' + item.flag + '_' + item.reportType, item.name );
			});

			currentPeriodOffset = 0;
			reportSelected();
			displayPeriodsInternal();
		});
	}
}

function changeExportType( value )
{
	if ( value == 0 )
	{
		byId( "exportReportDiv" ).style.height = "120px";
		byId( "exportReport" ).multiple = false;
		hideById( "periodTypeRow" );
		reportSelected();
	}
	else
	{
		byId( "exportReportDiv" ).style.height = "200px";
		byId( "exportReport" ).multiple = true;
		showById( "periodTypeRow" );
		reportSelected( getFieldValue( "periodType" ) );
	}
	
	displayPeriodsInternal();
	showById( "periodRow" );
}

function reportSelected( _periodType )
{
	if ( _periodType )
	{
		currentPeriodTypeName = _periodType;
	}
	else if ( getFieldValue( "multiExport" ) == 0 )
	{
		var value = getFieldValue( 'exportReport' );

		if ( value && value != null )
		{
			currentPeriodTypeName = (value.split( '_' )[1] == "true") ? 'Daily' : 'Monthly';
			currentReportTypeName = value.split( '_' )[2];

			if ( currentReportTypeName == "P" ) {
				hideById( "periodRow" );
			}else {
				showById( "periodRow" );
			}
		}
	}
	
	displayPeriodsInternal();
}

function displayPeriodsInternal()
{
	if ( currentPeriodTypeName )
	{
		var periods = periodTypeFactory.get( currentPeriodTypeName ).generatePeriods( currentPeriodOffset );
		periods = periodTypeFactory.filterFuturePeriods( periods );

		clearListById( 'selectedPeriodId' );

		for ( i in periods )
		{
			addOptionById( 'selectedPeriodId', periods[i].id, periods[i].name );
		}
	}
}

function getNextPeriod()
{
    if ( currentPeriodOffset < 0 ) // Cannot display future periods
    {
        currentPeriodOffset++;
        displayPeriodsInternal();
    }
}

function getPreviousPeriod()
{
    currentPeriodOffset--;
    displayPeriodsInternal();
}

function validateGenerateReport( isAdvanced )
{
	var exportReports = jQuery( 'select[id=exportReport]' ).children( 'option:selected' );

	if ( exportReports.length == 0 )
	{
		showErrorMessage( i18n_specify_export_report );
		return;
	}
	
	var url = 'validateGenerateReport.action?';
	
	jQuery.each( exportReports, function ( i, item )
	{
		url += 'exportReportIds=' + item.value.split( "_" )[0] + '&';
	} );
	
	url = url.substring( 0, url.length - 1 );
	
	if ( url && url != '' )
	{
		lockScreen();

		jQuery.postJSON( url,
		{
			'periodIndex': getFieldValue( 'selectedPeriodId' )
		},
		function( json )
		{
			if ( json.response == "success" ) {
				if ( isAdvanced ) {
					generateAdvancedExportReport();
				}
				else generateExportReport();
			}
			else {
				unLockScreen();
				showWarningMessage( json.message );
			}
		});
	}
}

function generateExportReport() {
		
	jQuery.postJSON( 'generateExportReport.action', {}, function ( json ) {
		if ( json.response == "success" ) {
			window.location = "downloadFile.action";		
			unLockScreen();
		}
	});
}

function getALLExportReportByGroup() {

	jQuery.postJSON( "getALLExportReportByGroup.action", {
		group: byId("group").value
	}, function( json ) {
		clearListById( 'exportReport' );
		var list = json.exportReports;
		
		for ( var i = 0 ; i < list.length ; i++ )
		{
			addOption( 'exportReport', item[i].name, item[i].id );
		}
	} );
}

function generateAdvancedExportReport()
{
	jQuery.postJSON( 'generateAdvancedExportReport.action', {
		organisationGroupId: byId("availableOrgunitGroups").value
	}, function( json ) {
		if ( json.response == "success" ) {
			showSuccessMessage( json.message );
		}
	} );
}
