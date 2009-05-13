
function generateResourceTable()
{
    var organisationUnit = document.getElementById( "organisationUnit" ).checked;
    var groupSet = document.getElementById( "groupSet" ).checked;
    var exclusiveGroupSet = document.getElementById( "exclusiveGroupSet" ).checked;
    var categoryOptionComboName = document.getElementById( "categoryOptionComboName" ).checked;
    
    if ( organisationUnit || groupSet || exclusiveGroupSet || categoryOptionComboName )
    {
        setMessage( i18n_generating_resource_tables );
            
        var params = "organisationUnit=" + organisationUnit + 
            "&groupSet=" + groupSet + 
            "&exclusiveGroupSet=" + exclusiveGroupSet +
            "&categoryOptionComboName=" + categoryOptionComboName;
            
        var url = "generateResourceTable.action";
        
        var request = new Request();
        request.sendAsPost( params );
        request.setCallbackSuccess( generateResourceTableReceived );
        request.send( url );
    }
    else
    {
        setMessage( i18n_select_options );
    }
}

function generateResourceTableReceived( messageElement )
{
    setMessage( i18n_resource_tables_generated );
}
