
$( document ).ready( function() {
	datePickerInRange( 'startDate' , 'endDate' );
	pingNotificationsTimeout();
} );

function startExport()
{
	$( '#notificationTable' ).show().prepend( '<tr><td>' + _loading_bar_html + '</td></tr>' );
	
	var url = 'startExport.action?startDate=' + $( '#startDate' ).val() + '&endDate=' + $( '#endDate' ).val();
	
	$( 'input[name="periodTypes"]').each( function() 
	{
		if ( $( this ).is( ':checked' ) )
		{
			url += "&periodTypes=" + $( this ).val();
		}
	} );
	
	var data = {
		'analytics': $( '#analytics' ).is( ':checked' ),
		'dataMart': $( '#dataMart' ).is( ':checked' )
	};
	
	$.get( url, data, pingNotificationsTimeout );
}

function pingNotificationsTimeout()
{
	pingNotifications( 'DATAMART', 'notificationTable' );
	setTimeout( "pingNotificationsTimeout()", 2500 );
}
