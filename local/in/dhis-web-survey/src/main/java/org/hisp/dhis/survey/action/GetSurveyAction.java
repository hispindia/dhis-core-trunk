package org.hisp.dhis.survey.action;

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

import java.util.ArrayList;
import java.util.List;

import org.hisp.dhis.dataset.DataEntryForm;
import org.hisp.dhis.indicator.Indicator;
import org.hisp.dhis.survey.Survey;
import org.hisp.dhis.survey.SurveyService;

import com.opensymphony.xwork2.Action;

/**
 * @author Kristian
 * @version $Id: GetDataSetAction.java 6256 2008-11-10 17:10:30Z larshelg $
 */
public class GetSurveyAction
    implements Action
{
    private int surveyId;

    private Survey survey;

    private List<Indicator> surveyIndicators;

    @SuppressWarnings("unused")
    private DataEntryForm dataEntryForm;
    
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private SurveyService surveyService;

    public void setSurveyService( SurveyService surveyService )
    {
        this.surveyService = surveyService;
    }

    // -------------------------------------------------------------------------
    // Getters & Setters
    // -------------------------------------------------------------------------

    public int getSurveyId()
    {
        return surveyId;
    }

    public void setSurveyId( int surveyId )
    {
        this.surveyId = surveyId;
    }

    public Survey getSurvey()
    {
        return survey;
    }
    
    public void setSurvey( Survey survey )
    {
        this.survey = survey ;
    }

    public List<Indicator> getSurveyIndicators()
    {
        return surveyIndicators;
    }
    
    private String description;

    public void setDescription( String description )
    {
        this.description = description;
    }
    
    public String getDescription()
    {
        return description;
    }

    // -------------------------------------------------------------------------
    // Action
    // -------------------------------------------------------------------------

    public String execute()
        throws Exception
    {
    	survey = surveyService.getSurvey( surveyId );

    	surveyIndicators = new ArrayList<Indicator>( survey.getIndicators() );

        //Collections.sort( surveyIndicators, dataElementComparator );       
                	
        //displayPropertyHandler.handle( dataSetDataElements );

        //dataEntryForm = dataEntryFormService.getDataEntryFormByDataSet( dataSet );
        
        return SUCCESS;
    }
}
