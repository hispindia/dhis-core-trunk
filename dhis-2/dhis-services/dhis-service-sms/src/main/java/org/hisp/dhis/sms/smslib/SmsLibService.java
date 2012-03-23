package org.hisp.dhis.sms.smslib;

/*
 * Copyright (c) 2004-2011, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * * Neither the name of the HISP project nor the names of its contributors may
 *   be used to endorse or promote products derived from this software without
 *   specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hisp.dhis.sms.SmsServiceException;
import org.hisp.dhis.sms.config.BulkSmsGatewayConfig;
import org.hisp.dhis.sms.config.ClickatellGatewayConfig;
import org.hisp.dhis.sms.config.GenericHttpGatewayConfig;
import org.hisp.dhis.sms.config.SmsConfiguration;
import org.hisp.dhis.sms.config.SmsGatewayConfig;
import org.hisp.dhis.sms.outbound.OutboundSms;
import org.hisp.dhis.sms.outbound.OutboundSmsTransportService;
import org.smslib.AGateway;
import org.smslib.GatewayException;
import org.smslib.IOutboundMessageNotification;
import org.smslib.OutboundMessage;
import org.smslib.SMSLibException;
import org.smslib.Service;
import org.smslib.Service.ServiceStatus;

public class SmsLibService
    implements OutboundSmsTransportService
{
    private static final Log log = LogFactory.getLog( SmsLibService.class );

    private Map<String, String> gatewayMap = new HashMap<String, String>();

    private GateWayFactory gatewayFactory = new GateWayFactory();

    private SmsConfiguration config;

    private String message = "success";

    private final String BULK_GATEWAY = "bulk_gw";

    private final String CLICKATELL_GATEWAY = "clickatell_gw";

    private final String HTTP_GATEWAY = "http_gw";

    private final String MODEM_GATEWAY = "modem_gw";

    // -------------------------------------------------------------------------
    // Implementation methods
    // -------------------------------------------------------------------------

    @Override
    public boolean isEnabled()
    {
        return config != null && config.isEnabled();
    }

    @Override
    public Map<String, String> getGatewayMap()
    {
        if ( gatewayMap == null || gatewayMap.isEmpty() )
        {
            reloadConfig();
        }

        return gatewayMap;
    }

    @Override
    public void sendMessage( OutboundSms sms, String gatewayId )
        throws SmsServiceException
    {
        message = getServiceStatus();

        if ( message != null && (message.equals( "service_stopped" ) || message.equals( "service_stopping" )) )
        {
            message = "service_stopped_cannot_send_sms";

            return;
        }

        String recipient;

        Set<String> recipients = sms.getRecipients();

        if ( recipients.size() == 0 )
        {
            message = "there_is_no_recipient_cannot_send_sms";

            log.warn( "Trying to send sms without recipients: " + sms );
            return;
        }
        else if ( recipients.size() == 1 )
        {
            recipient = recipients.iterator().next();
        }
        else
        {
            recipient = createTmpGroup( recipients );
        }

        OutboundMessage outboundMessage = new OutboundMessage( recipient, sms.getMessage() );

        String longNumber = config.getLongNumber();

        if ( longNumber != null && !longNumber.isEmpty() )
        {
            outboundMessage.setFrom( longNumber );
        }

        boolean sent = false;

        try
        {
            log.debug( "Sending message " + sms );

            if ( gatewayId == null || gatewayId.isEmpty() )
            {
                sent = getService().sendMessage( outboundMessage );
            }
            else
            {
                sent = getService().sendMessage( outboundMessage, gatewayId );
            }
        }
        catch ( SMSLibException e )
        {
            message = "Unable to send message: " + sms + " " + e.getCause().getMessage();

            log.warn( "Unable to send message: " + sms, e );
            throw new SmsServiceException( "Unable to send message: " + sms, e );
        }
        catch ( IOException e )
        {
            message = "Unable to send message: " + sms + " " + e.getCause().getMessage();

            log.warn( "Unable to send message: " + sms, e );
            throw new SmsServiceException( "Unable to send message: " + sms, e );
        }
        catch ( InterruptedException e )
        {
            message = "Unable to send message: " + sms + " " + e.getCause().getMessage();

            log.warn( "Unable to send message: " + sms, e );
            throw new SmsServiceException( "Unable to send message: " + sms, e );
        }
        finally
        {
            if ( recipients.size() > 1 )
            {
                // Make sure we delete tmp. group
                removeGroup( recipient );
            }
        }

        if ( sent )
        {
            message = "success";
        }
        else
        {
            message = "message_not_sent";
            log.warn( "Message not sent" );
        }
    }

    @Override
    public void initialize( SmsConfiguration smsConfiguration )
        throws SmsServiceException
    {
        // FIXME: Implement a decent equals..
        // if (smsConfiguration.equals( config )) {
        // // nothing to do
        // return;
        // }

        log.debug( "Initializing SmsLib" );

        this.config = smsConfiguration;

        ServiceStatus status = getService().getServiceStatus();

        if ( status == ServiceStatus.STARTED || status == ServiceStatus.STARTING )
        {
            log.debug( "Stopping SmsLib" );
            stopService();
        }

        log.debug( "Loading configuration" );
        reloadConfig();

        log.debug( "Starting SmsLib" );
        startService();
    }

    @Override
    public void reloadConfig()
        throws SmsServiceException
    {
        Service service = Service.getInstance();

        service.setOutboundMessageNotification( new OutboundNotification() );

        service.getGateways().clear();

        AGateway gateway = null;

        // Add gateways
        if ( config.getGateways() == null || config.getGateways().isEmpty() )
        {
            message = "unable_load_configuration_cause_of_there_is_no_gateway";
        }

        for ( SmsGatewayConfig gatewayConfig : config.getGateways() )
        {
            try
            {
                gateway = gatewayFactory.create( gatewayConfig );

                service.addGateway( gateway );

                if ( gatewayConfig instanceof BulkSmsGatewayConfig )
                {
                    gatewayMap.put( BULK_GATEWAY, gateway.getGatewayId() );
                }
                else if ( gatewayConfig instanceof ClickatellGatewayConfig )
                {
                    gatewayMap.put( CLICKATELL_GATEWAY, gateway.getGatewayId() );
                }
                else if ( gatewayConfig instanceof GenericHttpGatewayConfig )
                {
                    gatewayMap.put( HTTP_GATEWAY, gateway.getGatewayId() );
                }
                else
                {
                    gatewayMap.put( MODEM_GATEWAY, gateway.getGatewayId() );
                }

                message = "success";

                log.debug( "Added gateway " + gatewayConfig.getName() );
            }
            catch ( GatewayException e )
            {
                message = "Unable to load gateway " + gatewayConfig.getName() + e.getCause().getMessage();

                log.warn( "Unable to load gateway " + gatewayConfig.getName(), e );
                throw new SmsServiceException( "Unable to load gateway" + gatewayConfig.getName(), e );
            }
        }
    }

    @Override
    public void startService()
    {
        if ( config.isEnabled() && (message != null && message.equals( "success" )) )
        {
            try
            {
                getService().startService();
            }
            catch ( SMSLibException e )
            {
                message = "Unable to start smsLib service " + e.getCause().getMessage();

                log.warn( "Unable to start smsLib service", e );
                throw new SmsServiceException( "Unable to start smsLib service", e );
            }
            catch ( IOException e )
            {
                message = "Unable to start smsLib service" + e.getCause().getMessage();

                log.warn( "Unable to start smsLib service", e );
                throw new SmsServiceException( "Unable to start smsLib service", e );
            }
            catch ( InterruptedException e )
            {
                message = "Unable to start smsLib service" + e.getCause().getMessage();

                log.warn( "Unable to start smsLib service", e );
                throw new SmsServiceException( "Unable to start smsLib service", e );
            }
        }
        else
        {
            message = "sms_unable_or_there_is_no_gatewat_service_not_started";

            log.debug( "Sms not enabled or there is no any gateway, won't start service" );
        }
    }

    @Override
    public void stopService()
    {
        message = "success";

        try
        {
            getService().stopService();
        }
        catch ( SMSLibException e )
        {
            message = "Unable to stop smsLib service " + e.getCause().getMessage();

            log.warn( "Unable to stop smsLib service", e );
            throw new SmsServiceException( "Unable to stop smsLib service", e );
        }
        catch ( IOException e )
        {
            message = "Unable to stop smsLib service" + e.getCause().getMessage();

            log.warn( "Unable to stop smsLib service", e );
            throw new SmsServiceException( "Unable to stop smsLib service", e );
        }
        catch ( InterruptedException e )
        {
            message = "Unable to stop smsLib service" + e.getCause().getMessage();

            log.warn( "Unable to stop smsLib service", e );
            throw new SmsServiceException( "Unable to stop smsLib service", e );
        }
    }

    @Override
    public String getServiceStatus()
    {
        ServiceStatus serviceStatus = getService().getServiceStatus();

        if ( serviceStatus == ServiceStatus.STARTED )
        {
            return "service_started";
        }
        else if ( serviceStatus == ServiceStatus.STARTING )
        {
            return "service_starting";
        }
        else if ( serviceStatus == ServiceStatus.STOPPED )
        {
            return "service_stopped";
        }
        else
        {
            return "service_stopping";
        }
    }

    @Override
    public String getMessageStatus()
    {
        return message;
    }

    // -------------------------------------------------------------------------
    // Supportive methods
    // -------------------------------------------------------------------------

    private Service getService()
    {
        return Service.getInstance();
    }

    private String createTmpGroup( Set<String> recipients )
    {
        String groupName = Thread.currentThread().getName();

        getService().createGroup( groupName );

        for ( String recepient : recipients )
        {
            getService().addToGroup( groupName, recepient );
        }

        return groupName;
    }

    private void removeGroup( String groupName )
    {
        getService().removeGroup( groupName );
    }

    private class OutboundNotification
        implements IOutboundMessageNotification
    {
        @Override
        public void process( AGateway gateway, OutboundMessage msg )
        {
            log.debug( "Sent message through gateway " + gateway.getGatewayId() + ": " + msg );
        }
    }
}
