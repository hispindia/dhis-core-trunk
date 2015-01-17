package org.hisp.dhis.dataapproval;

/*
 * Copyright (c) 2004-2015, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
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

import org.hisp.dhis.dataelement.DataElementCategoryOptionCombo;
import org.hisp.dhis.dataset.DataSet;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.period.Period;

import java.util.List;
import java.util.Set;

/**
 * Defines the functionality for persisting DataApproval objects.
 *
 * @author Jim Grace
 */
public interface DataApprovalStore
//        extends GenericStore<DataApproval>
{
    String ID = DataApprovalStore.class.getName();

    // -------------------------------------------------------------------------
    // Basic DataApproval
    // -------------------------------------------------------------------------

    /**
     * Adds a DataApproval in order to approve data.
     *
     * @param dataApproval the DataApproval to add.
     */
    void addDataApproval( DataApproval dataApproval );

    /**
     * Updates a DataApproval.
     *
     * @param dataApproval the DataApproval to update.
     */
    void updateDataApproval( DataApproval dataApproval );

    /**
     * Deletes a DataApproval in order to un-approve data.
     *
     * @param dataApproval the DataApproval to delete.
     */
    void deleteDataApproval( DataApproval dataApproval );

    /**
     * Returns the DataApproval object (if any) for a given approval level,
     * dataset, period, organisation unit, and attribute option combo.
     *
     * @param dataApprovalLevel Level for approval
     * @param dataSet DataSet for approval
     * @param period Period for approval
     * @param organisationUnit OrganisationUnit for approval
     * @param attributeOptionCombo attribute option combo for approval
     * @return matching DataApproval object, if any
     */
    DataApproval getDataApproval( DataApprovalLevel dataApprovalLevel, DataSet dataSet, Period period,
        OrganisationUnit organisationUnit, DataElementCategoryOptionCombo attributeOptionCombo );

    /**
     * Returns a list of data approval results and corresponding states for
     * a collection of data sets and a given period. The list may be constrained
     * to a given organisation unit, or it may be all the organisation units
     * the user is allowed to see. The list may also be constrained to a given
     * attribute category combination, or it may be all the attribute category
     * combos the user is allowed to see.
     *
     * @param dataSets Data sets to look within
     * @param period Period to look within
     * @param orgUnit Organisation unit to look for (null means all)
     * @param attributeOptionCombo Attribute option combo (null means all)
     * @return data approval status objects
     */
    List<DataApprovalStatus> getDataApprovals( Set<DataSet> dataSets, Period period,
        OrganisationUnit orgUnit, DataElementCategoryOptionCombo attributeOptionCombo );
}
