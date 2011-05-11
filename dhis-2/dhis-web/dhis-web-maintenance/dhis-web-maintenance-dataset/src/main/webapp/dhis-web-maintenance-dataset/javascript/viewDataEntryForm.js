jQuery(document).ready(function() {
	validation2('saveDataEntryForm', function() {
		autoSave = false;
		validateDataEntryForm();
	}, {
		'rules' : getValidationRules("dataEntry")
	});

	leftBar.hideAnimated();
});

function localFilterSelectList( filter )
{
	if( jQuery("#dataElementsTab").is(":visible") ) {
		filterSelectList( "dataElementSelector", filter )
	} else {
		filterSelectList( "indicatorSelector", filter )
	}
}

function filterSelectList( select_id, filter )
{
	var select_selector = "#" + select_id;
	var select_hidden_id = select_id + "_ghost"
	var select_hidden_selector = "#" + select_hidden_id;

	if( $(select_hidden_selector).length === 0 ) {
		var $element = $("<select multiple=\"multiple\" id=\"" + select_hidden_id + "\" style=\"display: none\"></select>");
		$element.appendTo( "body" );
	}

	$(select_selector).find("option").each(function() {
		var val = $(this).val().toLowerCase();

		if(val.indexOf( filter ) == -1) {
			var $option = $(this).detach();
			$option.appendTo( select_hidden_selector );
		}
	});

	$(select_hidden_selector).find("option").each(function() {
		var val = $(this).val().toLowerCase();

		if(val.indexOf( filter ) != -1) {
			var $option = $(this).detach();
			$option.appendTo( select_selector );
		}
	});
}

function showThenFadeOutMessage( message )
{
	jQuery("#message_").html(message);
	jQuery("#message_").fadeOut(1000, function() {
		jQuery("#message_").html("");
		jQuery("#message_").show();
	});
}

function insertIndicator() {
	var oEditor = $("#designTextarea").ckeditorGet();
	var $option = $("#indicatorSelector option:selected").first();

	if( $option !== undefined )
	{
		var id = $option.data("id");
		var title = $option.val();
		var template = '<input id="indicator' + id + '" value="[ ' + title + ' ]" title="' + title + '" name="indicator" indicatorid="' + id + '" style="width:10em;text-align:center;" readonly="readonly" />';

		if(!checkExisted("indicator" + id)) {
			oEditor.insertHtml( template )
		} else {
			showThenFadeOutMessage( "<b>" + i18n_indicator_already_inserted + "</b>" );
		}
	} else {
		showThenFadeOutMessage( "<b>" + i18n_no_indicator_was_selected + "</b>" );
	}
}

function insertDataElement() {
	var oEditor = $("#designTextarea").ckeditorGet();
	var $option = $("#dataElementSelector option:selected").first();

	if( $option !== undefined )
	{
		var dataElementId = $option.data("dataelement-id");
		var dataElementName = $option.data("dataelement-name");
		var dataElementType = $option.data("dataelement-type");
		var optionComboId = $option.data("optioncombo-id");
		var optionComboName = $option.data("optioncombo-name");
	
		var titleValue = dataElementId + " - " + dataElementName + " - "
				+ optionComboId + " - " + optionComboName + " - " + dataElementType;
	
		var displayName = "[ " + dataElementName + " " + optionComboName + " ]";
		var dataEntryId = "value[" + dataElementId + "].value:value["
				+ optionComboId + "].value";
		var boolDataEntryId = "value[" + dataElementId + "].value:value["
				+ optionComboId + "].value";
	
		var id = "";
		var html = "";
	
		if (dataElementType == "bool") {
			id = boolDataEntryId;
			html = "<input title=\"" + titleValue
					+ "\" value=\"" + displayName + "\" id=\"" + boolDataEntryId
					+ "\" style=\"width:10em;text-align:center\"/>";
		} 
		else {
			id = dataEntryId;
			html = "<input title=\"" + titleValue
					+ "\" value=\"" + displayName + "\" id=\"" + dataEntryId
					+ "\" style=\"width:10em;text-align:center\"/>";
		}
	
		if (!checkExisted(id)) {
			oEditor.insertHtml(html);
		} else {
			showThenFadeOutMessage( "<b>" + i18n_dataelement_already_inserted + "</b>" );
		}
	} else {
		showThenFadeOutMessage( "<b>" + i18n_no_dataelement_was_selected + "</b>" );
	}
}

function checkExisted(id) {
	var result = false;
	var html = $("#designTextarea").ckeditorGet().getData();
	var input = jQuery(html).find("select, :text");
	input.each(function(i, item) {
		if (id == item.id)
			result = true;
	});

	return result;
}
