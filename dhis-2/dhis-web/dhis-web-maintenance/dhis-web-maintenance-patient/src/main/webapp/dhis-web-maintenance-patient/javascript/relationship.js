// -----------------------------------------------------------------------------
// View details
// -----------------------------------------------------------------------------

function showRelationshipTypeDetails( relationshipTypeId )
{
  	$.ajax({
		url: 'getRelationshipType.action?id=' + relationshipTypeId,
		cache: false,
		dataType: "xml",
		success: relationshipTypeReceived
	});
}

function relationshipTypeReceived( relationshipTypeElement )
{
	setInnerHTML( 'idField', getElementValue( relationshipTypeElement, 'id' ) );
	setInnerHTML( 'aIsToBField', getElementValue( relationshipTypeElement, 'aIsToB' ) );	
	setInnerHTML( 'bIsToAField', getElementValue( relationshipTypeElement, 'bIsToA' ) );       
	setInnerHTML( 'descriptionField', getElementValue( relationshipTypeElement, 'description' ) );
   
    showDetails();
}

// -----------------------------------------------------------------------------
// Add RelationshipType
// -----------------------------------------------------------------------------

function validateAddRelationshipType()
{
	$.postJSON(
    	    'validateRelationshipType.action',
    	    {
    	        "aIsToB": getFieldValue( 'aIsToB' ),
				"bIsToA": getFieldValue( 'bIsToA' )
    	    },
    	    function( json )
    	    {
    	    	if ( json.response == "success" )
    	    	{
					var form = document.getElementById( 'addRelationshipTypeForm' );        
					form.submit();
    	    	}else if ( json.response == "input" )
    	    	{
    	    		setHeaderMessage( json.message );
    	    	}
    	    	else if ( json.response == "error" )
    	    	{
    	    		setHeaderMessage( "i18n_adding_patient_atttibute_failed + ':' + '\n'" +json.message );
    	    	}
    	    }
    	);
}

// -----------------------------------------------------------------------------
// Update RelationshipType
// -----------------------------------------------------------------------------

function validateUpdateRelationshipType()
{
	$.postJSON(
    	    'validateRelationshipType.action',
    	    {
				"id": getFieldValue( 'id' ),
    	        "aIsToB": getFieldValue( 'aIsToB' ),
				"bIsToA": getFieldValue( 'bIsToA' )
    	    },
    	    function( json )
    	    {
    	    	if ( json.response == "success" )
    	    	{
					var form = document.getElementById( 'updateRelationshipTypeForm' );        
					form.submit();
    	    	}else if ( json.response == "input" )
    	    	{
    	    		setHeaderMessage( json.message );
    	    	}
    	    	else if ( json.response == "error" )
    	    	{
    	    		setHeaderMessage( "i18n_adding_patient_atttibute_failed + ':' + '\n'" +json.message );
    	    	}
    	    }
    	);
}

// -----------------------------------------------------------------------------
// Remove RelationshipType
// -----------------------------------------------------------------------------	

function removeRelationshipType( relationshipTypeId, aIsToB, bIsToA )
{
    removeItem( relationshipTypeId, aIsToB + "/" + bIsToA, i18n_confirm_delete, 'removeRelationshipType.action' );
}

//------------------------------------------------------------------------------
// Add Relationship
//------------------------------------------------------------------------------

function showAddRelationship( patientId )
{
	hideById('listRelationshipDiv');
	
	jQuery('#loaderDiv').show();
	jQuery('#addRelationshipDiv').load('showAddRelationshipForm.action',
		{
			patientId:patientId
		}, function()
		{
			showById('addRelationshipDiv');
			jQuery('#loaderDiv').hide();
		});
}

//-----------------------------------------------------------------------------
// Search Relationship Partner
//-----------------------------------------------------------------------------

function validateSearchPartner()
{	
	var request = new Request();
	request.setResponseTypeXML( 'message' );
	request.setCallbackSuccess( searchValidationCompleted );    
	request.sendAsPost(getParamsForDiv('relationshipSelectForm'));
	request.send( 'validateSearch.action' );        

	return false;
}

function searchValidationCompleted( messageElement )
{
	var type = messageElement.getAttribute( 'type' );
	var message = messageElement.firstChild.nodeValue;
	
	if( type == 'success' )
	{
		jQuery('#loaderDiv').show();
		jQuery("#relationshipSelectForm :input").each(function()
			{
				jQuery(this).attr('disabled', 'disabled');
			});
			
		$.ajax({
			type: "GET",
			url: 'searchRelationshipPatient.action',
			data: getParamsForDiv('relationshipSelectForm'),
			success: function( json ) {
				clearListById('availablePartnersList');
				for ( i in json.patients ) 
				{
					addOptionById( 'availablePartnersList', json.patients[i].id, json.patients[i].fullName );
				} 
				
				jQuery("#relationshipSelectForm :input").each(function()
					{
						jQuery(this).removeAttr('disabled');
					});
					
				jQuery('#loaderDiv').hide();
			}
		});
		return false;
	}
	else if( type == 'error' )
	{
		window.alert( i18n_searching_patient_failed + ':' + '\n' + message );
	}
	else if( type == 'input' )
	{
		setHeaderMessage( message );
	}
}

function addRelationship() 
{
	var relationshipTypeId = getFieldValue( 'relationshipTypeId' );
	
	var partnerList = document.getElementById( 'availablePartnersList' );
	var partnerId = -1;
	
	if( partnerList.selectedIndex >= 0 )
	{		
		partnerId = partnerList.options[partnerList.selectedIndex].value;		
	}	
	
	if( relationshipTypeId == "null" || relationshipTypeId == "" )
	{
		window.alert( i18n_please_select_relationship_type );
		
		return;
	}
	
	if( partnerId == "null" || partnerId == "" || partnerId < 0 )
	{
		window.alert( i18n_please_select_partner );
		
		return;
	}
	
	var relTypeId = relationshipTypeId.substr( 0, relationshipTypeId.indexOf(':') );
	var relName = relationshipTypeId.substr( relationshipTypeId.indexOf(':') + 1, relationshipTypeId.length );
	
	var url = 'saveRelationship.action?' + 
		'patientId=' + getFieldValue('patientId') + 
		'&partnerId=' + partnerId + 
		'&relationshipTypeId=' + relTypeId +
		'&relationshipName=' + relName ;
	
	jQuery('#loaderDiv').show();
	var request = new Request();
	request.setResponseTypeXML( 'message' );
	request.setCallbackSuccess( addRelationshipCompleted );    
	request.send( url );
	
	return false;
	
}

function addRelationshipCompleted( messageElement )
{
	var type = messageElement.getAttribute( 'type' );
	var message = messageElement.firstChild.nodeValue;
	
	if( type == 'success' )
	{
		showSuccessMessage( i18n_save_success );
	}	
	else if( type == 'error' )
	{
		showErrorMessage( i18n_adding_relationship_failed + ':' + '\n' + message );
	}
	else if( type == 'input' )
	{
		showWarningMessage( message );
	}
	jQuery('#loaderDiv').hide();
}

//------------------------------------------------------------------------------
// Remove Relationship
//------------------------------------------------------------------------------

function removeRelationship( relationshipId, patientA, aIsToB, patientB )
{	
	removeItem( relationshipId, patientA + ' is ' + aIsToB + ' to ' + patientB, i18n_confirm_delete_relationship, 'removeRelationship.action' );
}

//------------------------------------------------------------------------------
// Relationship partner
//------------------------------------------------------------------------------

function manageRepresentative( patientId, partnerId )
{
    var request = new Request();
    request.setResponseTypeXML( 'partner' );
    request.setCallbackSuccess( representativeReceived );
    request.send( 'getPartner.action?patientId=' + patientId + '&partnerId=' + partnerId );
}

function representativeReceived( patientElement )
{		
	var partnerIsRepresentative = getElementValue( patientElement, 'partnerIsRepresentative' );	
	
	var patientId = getFieldValue('id' );
	var partnerId = getElementValue( patientElement, 'id' );
	var labelField;	
	var buttonFirstField;
	var buttonSecondField;
	
	if( partnerIsRepresentative == 'true' )
	{
		labelField = i18n_do_you_want_to_remove_this_one_from_being_representative;
		
		buttonFirstField = '<input type="button" value="' + i18n_yes + '" onclick="javascript:removeRepresentative( ' + patientId + ',' + partnerId + ')">'; 
		buttonSecondField = '&nbsp;';
	}
	else if( partnerIsRepresentative == 'false' )
	{
		labelField = i18n_do_you_want_to_make_this_one_a_representative;
		
		buttonFirstField = '<input type="button" value="' + i18n_yes + '" onclick="javascript:saveRepresentative( ' + patientId + ',' + partnerId + ', false )">';
		buttonSecondField= '<input type="button" value="' + i18n_yes_and_attribute + '" onclick="javascript:saveRepresentative( ' + patientId + ',' + partnerId + ', true )">';
	}	
	
	setInnerHTML( 'labelField', labelField );
	setInnerHTML( 'buttonFirstField', buttonFirstField );
	setInnerHTML( 'buttonSecondField', buttonSecondField );
	setInnerHTML( 'fullNameField', getElementValue( patientElement, 'fullName' ) );
	setInnerHTML( 'genderField', getElementValue( patientElement, 'gender' ) );	
    setInnerHTML( 'dateOfBirthField', getElementValue( patientElement, 'dateOfBirth' ) );    
    setInnerHTML( 'ageField', getElementValue( patientElement, 'age' ) );
	
	var attributes = patientElement.getElementsByTagName( "attribute" );   
    
    var attributeValues = '';
	
	for( var i = 0; i < attributes.length; i++ )
	{		
		attributeValues = attributeValues + '<strong>' + attributes[ i ].getElementsByTagName( "name" )[0].firstChild.nodeValue  + ':  </strong>' + attributes[ i ].getElementsByTagName( "value" )[0].firstChild.nodeValue + '<br>';		
	}
	
	setInnerHTML( 'attributeField', attributeValues );   
   
    showPartnerDetail( true );
}

function showPartnerDetail( display )
{   
    var node = document.getElementById( 'relationshipPartnerContainer' );
    
    node.style.display = (display ? 'block' : 'none');   
}


function hideRelationshipPartnerContainer()
{   
    var node = document.getElementById( 'relationshipPartnerContainer' );
    
    node.style.display = 'none';   
}

function saveRepresentative( patientId, representativeId, copyAttribute )
{
	var url = 'saveRepresentative.action'
	var params  = 'patientId=' + patientId
		params += '&representativeId=' + representativeId;	
		params += '&copyAttribute=' + copyAttribute;
	
	
	var request = new Request();
	request.setResponseTypeXML( 'message' );
	request.setCallbackSuccess( saveRepresentativeCompleted ); 
	request.sendAsPost( params );	
	request.send( url );        

	return false;
}

function saveRepresentativeCompleted( messageElement )
{
	var type = messageElement.getAttribute( 'type' );
	var message = messageElement.firstChild.nodeValue;
	
	if( type == 'success' )
	{
		hideById('relationshipPartnerContainer');
	}	
	else if( type == 'error' )
	{
		showErrorMessage( i18n_saving_representative_failed + ':' + '\n' + message );
	}
	else if( type == 'input' )
	{
		showWarningMessage( message );
	}
}

function removeRepresentative( patientId, representativeId )
{	
	var url = 'removeRepresentative.action';
	var params = 'patientId=' + patientId;
		params +='&representativeId=' + representativeId;	
	
	var request = new Request();
	request.setResponseTypeXML( 'message' );
	request.setCallbackSuccess( removeRepresentativeCompleted );
	request.sendAsPost( params );
	request.send( url );        

	return false;
	
}

function removeRepresentativeCompleted( messageElement )
{
	var type = messageElement.getAttribute( 'type' );
	var message = messageElement.firstChild.nodeValue;
	
	if( type == 'success' )
	{
		showRelationshipList( getFieldValue('id') );
	}	
	else if( type == 'error' )
	{
		window.alert( i18n_removing_representative_failed + ':' + '\n' + message );
	}
	else if( type == 'input' )
	{
		setHeaderMessage( message );
	}
}
