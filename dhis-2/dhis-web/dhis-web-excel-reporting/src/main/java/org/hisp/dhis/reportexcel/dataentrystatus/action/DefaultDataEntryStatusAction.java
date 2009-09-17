/*
 * Copyright (c) 2004-2007, University of Oslo
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
package org.hisp.dhis.reportexcel.dataentrystatus.action;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hisp.dhis.dataset.CompleteDataSetRegistration;
import org.hisp.dhis.dataset.CompleteDataSetRegistrationService;
import org.hisp.dhis.dataset.DataSet;
import org.hisp.dhis.dataset.DataSetService;
import org.hisp.dhis.i18n.I18nFormat;
import org.hisp.dhis.ouwt.manager.OrganisationUnitSelectionManager;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.period.PeriodService;
import org.hisp.dhis.period.PeriodType;
import org.hisp.dhis.period.comparator.PeriodComparator;
import org.hisp.dhis.reportexcel.ReportExcelService;
import org.hisp.dhis.reportexcel.status.DataEntryStatus;
import org.hisp.dhis.reportexcel.utils.DateUtils;
import org.hisp.dhis.system.util.TimeUtils;
import org.hisp.dhis.user.CurrentUserService;
import org.hisp.dhis.user.UserAuthorityGroup;
import org.hisp.dhis.user.UserCredentials;
import org.hisp.dhis.user.UserStore;
import com.opensymphony.xwork2.Action;

/**
 * @author Tran Thanh Tri
 * @version $Id$
 */
public class DefaultDataEntryStatusAction
    implements Action
{
    // -------------------------------------------------
    // Dependency
    // -------------------------------------------------

    private ReportExcelService reportService;

    private CurrentUserService currentUserService;

    private UserStore userStore;

    private OrganisationUnitSelectionManager selectionManager;

    private DataSetService dataSetService;

    private PeriodService periodService;

    private CompleteDataSetRegistrationService completeDataSetRegistrationService;

    private I18nFormat format;

    // -------------------------------------------------
    // Output
    // -------------------------------------------------

    private List<DataEntryStatus> dataStatus;

    private Map<DataSet, List<DataEntryStatus>> maps;

    private List<DataSet> dataSets;

    // -------------------------------------------------
    // Getter & Setter
    // -------------------------------------------------

    public void setDataSetService( DataSetService dataSetService )
    {
        this.dataSetService = dataSetService;
    }

    public void setFormat( I18nFormat format )
    {
        this.format = format;
    }

    public Map<DataSet, List<DataEntryStatus>> getMaps()
    {
        return maps;
    }

    public List<DataSet> getDataSets()
    {
        return dataSets;
    }

    public void setPeriodService( PeriodService periodService )
    {
        this.periodService = periodService;
    }

    public void setCompleteDataSetRegistrationService(
        CompleteDataSetRegistrationService completeDataSetRegistrationService )
    {
        this.completeDataSetRegistrationService = completeDataSetRegistrationService;
    }

    public void setSelectionManager( OrganisationUnitSelectionManager selectionManager )
    {
        this.selectionManager = selectionManager;
    }

    public void setUserStore( UserStore userStore )
    {
        this.userStore = userStore;
    }

    public List<DataEntryStatus> getDataStatus()
    {
        return dataStatus;
    }

    public void setReportService( ReportExcelService reportService )
    {
        this.reportService = reportService;
    }

    public void setCurrentUserService( CurrentUserService currentUserService )
    {
        this.currentUserService = currentUserService;
    }

    public I18nFormat getFormat()
    {
        return format;
    }

    public String execute()
        throws Exception
    {

        if ( selectionManager.getSelectedOrganisationUnit() != null )
        {
            TimeUtils.start();
            dataSets = new ArrayList<DataSet>( dataSetService.getDataSetsBySource( selectionManager
                .getSelectedOrganisationUnit() ) );
            TimeUtils.markHMS( "datasets" );
            TimeUtils.stop();
            if ( !currentUserService.currentUserIsSuper() )
            {
                UserCredentials userCredentials = userStore.getUserCredentials( currentUserService.getCurrentUser() );

                Set<DataSet> dataSetUserAuthorityGroups = new HashSet<DataSet>();

                for ( UserAuthorityGroup userAuthorityGroup : userCredentials.getUserAuthorityGroups() )
                {
                    dataSetUserAuthorityGroups.addAll( userAuthorityGroup.getDataSets() );
                }

                dataSets.retainAll( dataSetUserAuthorityGroups );
            }

            dataStatus = new ArrayList<DataEntryStatus>( reportService
                .getDataEntryStatusDefaultByDataSets( dataSets ) );

            maps = new HashMap<DataSet, List<DataEntryStatus>>();

            Calendar calendar = Calendar.getInstance();

            for ( DataEntryStatus d : dataStatus )
            {
                d.setNumberOfDataElement( d.getDataSet().getDataElements().size() );

                PeriodType periodType = d.getPeriodType();

                List<Period> periods = new ArrayList<Period>( periodService.getPeriodsBetweenDates( periodType,
                    DateUtils.getFirstDayOfYear( calendar.get( Calendar.YEAR ) ), DateUtils.getLastDayOfYear( calendar
                        .get( Calendar.YEAR ) ) ) );
                List<DataEntryStatus> ds_temp = new ArrayList<DataEntryStatus>();
                Collections.sort( periods, new PeriodComparator() );
                for ( Period p : periods )
                {

                    DataEntryStatus dataStatusNew = new DataEntryStatus();
                    dataStatusNew.setPeriod( p );
                    dataStatusNew.setNumberOfDataElement( d.getNumberOfDataElement() );
                    dataStatusNew.setNumberOfDataValue( reportService.countDataValueOfDataSet( d.getDataSet(),
                        selectionManager.getSelectedOrganisationUnit(), p ) );

                    CompleteDataSetRegistration completeDataSetRegistration = completeDataSetRegistrationService
                        .getCompleteDataSetRegistration( d.getDataSet(), p, selectionManager
                            .getSelectedOrganisationUnit() );

                    dataStatusNew.setCompleted( (completeDataSetRegistration == null ? false : true) );

                    ds_temp.add( dataStatusNew );

                }
                maps.put( d.getDataSet(), ds_temp );
            }
        }
        return SUCCESS;
    }
}
