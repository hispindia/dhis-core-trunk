package org.hisp.dhis.dataset.action.dataentryform;

/*
 * Copyright (c) 2004-2012, University of Oslo
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

import org.hisp.dhis.dataentryform.DataEntryForm;
import org.hisp.dhis.dataentryform.DataEntryFormService;
import org.hisp.dhis.dataset.DataSet;
import org.hisp.dhis.dataset.DataSetService;

import com.opensymphony.xwork2.Action;

/**
 * @author Bharath Kumar
 * @version $Id$
 */
public class SaveDataEntryFormAction
    implements Action
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private DataEntryFormService dataEntryFormService;

    public void setDataEntryFormService( DataEntryFormService dataEntryFormService )
    {
        this.dataEntryFormService = dataEntryFormService;
    }   

    private DataSetService dataSetService;

    public void setDataSetService( DataSetService dataSetService )
    {
        this.dataSetService = dataSetService;
    }

    // -------------------------------------------------------------------------
    // Getters & Setters
    // -------------------------------------------------------------------------

    private int dataSetIdField;

    public void setDataSetIdField( int dataSetIdField )
    {
        this.dataSetIdField = dataSetIdField;
    }

    private String nameField;

    public void setNameField( String nameField )
    {
        this.nameField = nameField;
    }

    private String designTextarea;

    public void setDesignTextarea( String designTextarea )
    {
        this.designTextarea = designTextarea;
    }

    // -------------------------------------------------------------------------
    // Execute
    // -------------------------------------------------------------------------

    public String execute()
        throws Exception
    {
        DataSet dataset = dataSetService.getDataSet( dataSetIdField );

        designTextarea = dataEntryFormService.prepareDataEntryFormForSave( designTextarea );
        
        DataEntryForm dataEntryForm = dataset.getDataEntryForm();

        if ( !( dataEntryForm != null && dataEntryForm.getHtmlCode().equals( designTextarea ) ) )
        {
            dataset.increaseVersion(); // Check if version must be updated
        }
        
        if ( dataEntryForm == null )
        {
            dataEntryForm = new DataEntryForm( nameField, dataEntryFormService.prepareDataEntryFormForSave( designTextarea ) );
            dataEntryFormService.addDataEntryForm( dataEntryForm );
            dataset.setDataEntryForm( dataEntryForm );
        }
        else
        {
            dataEntryForm.setName( nameField );
            dataEntryForm.setHtmlCode( dataEntryFormService.prepareDataEntryFormForSave( designTextarea ) );
            dataEntryFormService.updateDataEntryForm( dataEntryForm );
        }
        
        dataSetService.updateDataSet( dataset );

        return SUCCESS;
    }
}
