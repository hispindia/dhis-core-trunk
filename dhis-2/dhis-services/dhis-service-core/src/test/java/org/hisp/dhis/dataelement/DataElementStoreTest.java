package org.hisp.dhis.dataelement;

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

import org.hisp.dhis.DhisSpringTest;
import org.hisp.dhis.analytics.AggregationType;
import org.hisp.dhis.attribute.Attribute;
import org.hisp.dhis.attribute.AttributeService;
import org.hisp.dhis.attribute.AttributeValue;
import org.hisp.dhis.attribute.exception.NonUniqueAttributeValueException;
import org.hisp.dhis.common.ValueType;
import org.hisp.dhis.dataset.DataSet;
import org.hisp.dhis.dataset.DataSetService;
import org.hisp.dhis.period.MonthlyPeriodType;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Torgeir Lorange Ostby
 * @version $Id: DataElementStoreTest.java 5742 2008-09-26 11:37:35Z larshelg $
 */
public class DataElementStoreTest
    extends DhisSpringTest
{
    @Autowired
    private DataElementStore dataElementStore;

    @Autowired
    private DataSetService dataSetService;

    @Autowired
    private AttributeService attributeService;

    // -------------------------------------------------------------------------
    // Tests
    // -------------------------------------------------------------------------

    @Test
    public void testAddDataElement()
    {
        DataElement dataElementA = createDataElement( 'A' );
        DataElement dataElementB = createDataElement( 'B' );
        DataElement dataElementC = createDataElement( 'C' );

        int idA = dataElementStore.save( dataElementA );
        int idB = dataElementStore.save( dataElementB );
        int idC = dataElementStore.save( dataElementC );

        dataElementA = dataElementStore.get( idA );
        assertNotNull( dataElementA );
        assertEquals( idA, dataElementA.getId() );
        assertEquals( "DataElementA", dataElementA.getName() );

        dataElementB = dataElementStore.get( idB );
        assertNotNull( dataElementB );
        assertEquals( idB, dataElementB.getId() );
        assertEquals( "DataElementB", dataElementB.getName() );

        dataElementC = dataElementStore.get( idC );
        assertNotNull( dataElementC );
        assertEquals( idC, dataElementC.getId() );
        assertEquals( "DataElementC", dataElementC.getName() );
    }

    @Test
    public void testUpdateDataElement()
    {
        DataElement dataElementA = createDataElement( 'A' );
        int idA = dataElementStore.save( dataElementA );
        dataElementA = dataElementStore.get( idA );
        assertEquals( ValueType.INTEGER, dataElementA.getValueType() );

        dataElementA.setValueType( ValueType.BOOLEAN );
        dataElementStore.update( dataElementA );
        dataElementA = dataElementStore.get( idA );
        assertNotNull( dataElementA.getValueType() );
        assertEquals( ValueType.BOOLEAN, dataElementA.getValueType() );
    }

    @Test
    public void testDeleteAndGetDataElement()
    {
        DataElement dataElementA = createDataElement( 'A' );
        DataElement dataElementB = createDataElement( 'B' );
        DataElement dataElementC = createDataElement( 'C' );
        DataElement dataElementD = createDataElement( 'D' );

        int idA = dataElementStore.save( dataElementA );
        int idB = dataElementStore.save( dataElementB );
        int idC = dataElementStore.save( dataElementC );
        int idD = dataElementStore.save( dataElementD );

        assertNotNull( dataElementStore.get( idA ) );
        assertNotNull( dataElementStore.get( idB ) );
        assertNotNull( dataElementStore.get( idC ) );
        assertNotNull( dataElementStore.get( idD ) );

        dataElementA = dataElementStore.get( idA );
        dataElementB = dataElementStore.get( idB );
        dataElementC = dataElementStore.get( idC );
        dataElementD = dataElementStore.get( idD );

        dataElementStore.delete( dataElementA );
        assertNull( dataElementStore.get( idA ) );
        assertNotNull( dataElementStore.get( idB ) );
        assertNotNull( dataElementStore.get( idC ) );
        assertNotNull( dataElementStore.get( idD ) );

        dataElementStore.delete( dataElementB );
        assertNull( dataElementStore.get( idA ) );
        assertNull( dataElementStore.get( idB ) );
        assertNotNull( dataElementStore.get( idC ) );
        assertNotNull( dataElementStore.get( idD ) );

        dataElementStore.delete( dataElementC );
        assertNull( dataElementStore.get( idA ) );
        assertNull( dataElementStore.get( idB ) );
        assertNull( dataElementStore.get( idC ) );
        assertNotNull( dataElementStore.get( idD ) );

        dataElementStore.delete( dataElementD );
        assertNull( dataElementStore.get( idA ) );
        assertNull( dataElementStore.get( idB ) );
        assertNull( dataElementStore.get( idC ) );
        assertNull( dataElementStore.get( idD ) );
    }

    @Test
    public void testGetDataElementByName()
    {
        DataElement dataElementA = createDataElement( 'A' );
        DataElement dataElementB = createDataElement( 'B' );
        int idA = dataElementStore.save( dataElementA );
        int idB = dataElementStore.save( dataElementB );

        dataElementA = dataElementStore.getByName( "DataElementA" );
        assertNotNull( dataElementA );
        assertEquals( idA, dataElementA.getId() );
        assertEquals( "DataElementA", dataElementA.getName() );

        dataElementB = dataElementStore.getByName( "DataElementB" );
        assertNotNull( dataElementB );
        assertEquals( idB, dataElementB.getId() );
        assertEquals( "DataElementB", dataElementB.getName() );

        DataElement dataElementC = dataElementStore.getByName( "DataElementC" );
        assertNull( dataElementC );
    }

    @Test
    public void testGetDataElementByShortName()
    {
        DataElement dataElementA = createDataElement( 'A' );
        DataElement dataElementB = createDataElement( 'B' );
        int idA = dataElementStore.save( dataElementA );
        int idB = dataElementStore.save( dataElementB );

        dataElementA = dataElementStore.getByShortName( "DataElementShortA" );
        assertNotNull( dataElementA );
        assertEquals( idA, dataElementA.getId() );
        assertEquals( "DataElementA", dataElementA.getName() );

        dataElementB = dataElementStore.getByShortName( "DataElementShortB" );
        assertNotNull( dataElementB );
        assertEquals( idB, dataElementB.getId() );
        assertEquals( "DataElementB", dataElementB.getName() );

        DataElement dataElementC = dataElementStore.getByShortName( "DataElementShortC" );
        assertNull( dataElementC );
    }

    @Test
    public void testGetAllDataElements()
    {
        assertEquals( 0, dataElementStore.getAll().size() );

        DataElement dataElementA = createDataElement( 'A' );
        DataElement dataElementB = createDataElement( 'B' );
        DataElement dataElementC = createDataElement( 'C' );
        DataElement dataElementD = createDataElement( 'D' );

        dataElementStore.save( dataElementA );
        dataElementStore.save( dataElementB );
        dataElementStore.save( dataElementC );
        dataElementStore.save( dataElementD );

        List<DataElement> dataElementsRef = new ArrayList<>();
        dataElementsRef.add( dataElementA );
        dataElementsRef.add( dataElementB );
        dataElementsRef.add( dataElementC );
        dataElementsRef.add( dataElementD );

        List<DataElement> dataElements = dataElementStore.getAll();
        assertNotNull( dataElements );
        assertEquals( dataElementsRef.size(), dataElements.size() );
        assertTrue( dataElements.containsAll( dataElementsRef ) );
    }

    @Test
    public void testGetAggregateableDataElements()
    {
        assertEquals( 0, dataElementStore.getAggregateableDataElements().size() );

        DataElement dataElementA = createDataElement( 'A' );
        DataElement dataElementB = createDataElement( 'B' );
        DataElement dataElementC = createDataElement( 'C' );
        DataElement dataElementD = createDataElement( 'D' );

        dataElementA.setValueType( ValueType.INTEGER );
        dataElementB.setValueType( ValueType.BOOLEAN );
        dataElementC.setValueType( ValueType.TEXT );
        dataElementD.setValueType( ValueType.INTEGER );

        dataElementStore.save( dataElementA );
        dataElementStore.save( dataElementB );
        dataElementStore.save( dataElementC );
        dataElementStore.save( dataElementD );

        List<DataElement> dataElementsRef = new ArrayList<>();
        dataElementsRef.add( dataElementA );
        dataElementsRef.add( dataElementB );
        dataElementsRef.add( dataElementD );

        List<DataElement> dataElements = dataElementStore.getAggregateableDataElements();
        assertNotNull( dataElements );
        assertEquals( dataElementsRef.size(), dataElements.size() );
        assertTrue( dataElements.containsAll( dataElementsRef ) );
    }

    @Test
    public void testGetDataElementsByAggregationType()
    {
        assertEquals( 0, dataElementStore.getDataElementsByAggregationType( AggregationType.AVERAGE_SUM_ORG_UNIT ).size() );
        assertEquals( 0, dataElementStore.getDataElementsByAggregationType( AggregationType.SUM ).size() );

        DataElement dataElementA = createDataElement( 'A' );
        dataElementA.setAggregationType( AggregationType.AVERAGE_SUM_ORG_UNIT );
        DataElement dataElementB = createDataElement( 'B' );
        dataElementB.setAggregationType( AggregationType.SUM );
        DataElement dataElementC = createDataElement( 'C' );
        dataElementC.setAggregationType( AggregationType.SUM );
        DataElement dataElementD = createDataElement( 'D' );
        dataElementD.setAggregationType( AggregationType.SUM );

        dataElementStore.save( dataElementA );
        dataElementStore.save( dataElementB );
        dataElementStore.save( dataElementC );
        dataElementStore.save( dataElementD );

        assertEquals( 1, dataElementStore.getDataElementsByAggregationType( AggregationType.AVERAGE_SUM_ORG_UNIT ).size() );
        assertEquals( 3, dataElementStore.getDataElementsByAggregationType( AggregationType.SUM ).size() );
    }

    @Test
    public void testGetDataElementsByDomainType()
    {
        assertEquals( 0, dataElementStore.getDataElementsByDomainType( DataElementDomain.AGGREGATE ).size() );
        assertEquals( 0, dataElementStore.getDataElementsByDomainType( DataElementDomain.TRACKER ).size() );

        DataElement dataElementA = createDataElement( 'A' );
        dataElementA.setDomainType( DataElementDomain.AGGREGATE );
        DataElement dataElementB = createDataElement( 'B' );
        dataElementB.setDomainType( DataElementDomain.TRACKER );
        DataElement dataElementC = createDataElement( 'C' );
        dataElementC.setDomainType( DataElementDomain.TRACKER );
        DataElement dataElementD = createDataElement( 'D' );
        dataElementD.setDomainType( DataElementDomain.TRACKER );

        dataElementStore.save( dataElementA );
        dataElementStore.save( dataElementB );
        dataElementStore.save( dataElementC );
        dataElementStore.save( dataElementD );

        assertEquals( 1, dataElementStore.getDataElementsByDomainType( DataElementDomain.AGGREGATE ).size() );
        assertEquals( 3, dataElementStore.getDataElementsByDomainType( DataElementDomain.TRACKER ).size() );
    }

    @Test
    public void testGetDataElementAggregationLevels()
    {
        List<Integer> aggregationLevels = Arrays.asList( 3, 5 );

        DataElement dataElementA = createDataElement( 'A' );
        dataElementA.setAggregationLevels( aggregationLevels );

        int idA = dataElementStore.save( dataElementA );

        assertNotNull( dataElementStore.get( idA ).getAggregationLevels() );
        assertEquals( 2, dataElementStore.get( idA ).getAggregationLevels().size() );
        assertEquals( aggregationLevels, dataElementStore.get( idA ).getAggregationLevels() );
    }

    @Test
    public void testGetDataElementsByAggregationLevel()
    {
        DataElement dataElementA = createDataElement( 'A' );
        DataElement dataElementB = createDataElement( 'B' );
        DataElement dataElementC = createDataElement( 'C' );

        dataElementA.getAggregationLevels().addAll( Arrays.asList( 3, 5 ) );
        dataElementB.getAggregationLevels().addAll( Arrays.asList( 4, 5 ) );

        dataElementStore.save( dataElementA );
        dataElementStore.save( dataElementB );
        dataElementStore.save( dataElementC );

        List<DataElement> dataElements = dataElementStore.getDataElementsByAggregationLevel( 2 );

        assertEquals( 0, dataElements.size() );

        dataElements = dataElementStore.getDataElementsByAggregationLevel( 3 );

        assertEquals( 1, dataElements.size() );

        dataElements = dataElementStore.getDataElementsByAggregationLevel( 4 );

        assertEquals( 1, dataElements.size() );

        dataElements = dataElementStore.getDataElementsByAggregationLevel( 5 );

        assertEquals( 2, dataElements.size() );
        assertTrue( dataElements.contains( dataElementA ) );
        assertTrue( dataElements.contains( dataElementB ) );
    }

    @Test
    public void testGetDataElementsZeroIsSignificant()
    {
        DataElement dataElementA = createDataElement( 'A' );
        DataElement dataElementB = createDataElement( 'B' );
        DataElement dataElementC = createDataElement( 'C' );
        DataElement dataElementD = createDataElement( 'D' );

        dataElementA.setZeroIsSignificant( true );
        dataElementB.setZeroIsSignificant( true );

        dataElementStore.save( dataElementA );
        dataElementStore.save( dataElementB );
        dataElementStore.save( dataElementC );
        dataElementStore.save( dataElementD );

        List<DataElement> dataElements = dataElementStore.getDataElementsByZeroIsSignificant( true );

        assertTrue( equals( dataElements, dataElementA, dataElementB ) );
    }

    @Test
    public void testGetDataElements()
    {
        DataElement dataElementA = createDataElement( 'A' );
        DataElement dataElementB = createDataElement( 'B' );
        DataElement dataElementC = createDataElement( 'C' );
        DataElement dataElementD = createDataElement( 'D' );
        DataElement dataElementE = createDataElement( 'E' );
        DataElement dataElementF = createDataElement( 'F' );

        dataElementStore.save( dataElementA );
        dataElementStore.save( dataElementB );
        dataElementStore.save( dataElementC );
        dataElementStore.save( dataElementD );
        dataElementStore.save( dataElementE );
        dataElementStore.save( dataElementF );

        DataSet dataSetA = createDataSet( 'A', new MonthlyPeriodType() );
        DataSet dataSetB = createDataSet( 'B', new MonthlyPeriodType() );

        dataSetA.getDataElements().add( dataElementA );
        dataSetA.getDataElements().add( dataElementC );
        dataSetA.getDataElements().add( dataElementF );
        dataSetB.getDataElements().add( dataElementD );
        dataSetB.getDataElements().add( dataElementF );

        dataSetService.addDataSet( dataSetA );
        dataSetService.addDataSet( dataSetB );

        List<DataSet> dataSets = new ArrayList<>();
        dataSets.add( dataSetA );
        dataSets.add( dataSetB );

        List<DataElement> dataElements = dataElementStore.getDataElementsByDataSets( dataSets );

        assertNotNull( dataElements );
        assertEquals( 4, dataElements.size() );
        assertTrue( dataElements.contains( dataElementA ) );
        assertTrue( dataElements.contains( dataElementC ) );
        assertTrue( dataElements.contains( dataElementD ) );
        assertTrue( dataElements.contains( dataElementF ) );
    }

    @Test
    public void testDataElementFromAttribute() throws NonUniqueAttributeValueException
    {
        Attribute attribute = new Attribute( "test", ValueType.TEXT );
        attribute.setDataElementAttribute( true );
        attributeService.addAttribute( attribute );

        DataElement dataElementA = createDataElement( 'A' );
        DataElement dataElementB = createDataElement( 'B' );
        dataElementStore.save( dataElementA );
        dataElementStore.save( dataElementB );

        AttributeValue attributeValue = new AttributeValue( "SOME VALUE", attribute );
        attributeService.addAttributeValue( dataElementA, attributeValue );

        dataElementA.getAttributeValues().add( attributeValue );
        dataElementStore.update( dataElementA );

        DataElement dataElement = dataElementStore.getByAttribute( attribute );
        assertEquals( dataElement.getUid(), dataElementA.getUid() );
    }

    @Test
    public void testAttributeValueFromAttribute() throws NonUniqueAttributeValueException
    {
        Attribute attribute = new Attribute( "test", ValueType.TEXT );
        attribute.setDataElementAttribute( true );
        attributeService.addAttribute( attribute );

        DataElement dataElementA = createDataElement( 'A' );
        DataElement dataElementB = createDataElement( 'B' );
        DataElement dataElementC = createDataElement( 'C' );

        dataElementStore.save( dataElementA );
        dataElementStore.save( dataElementB );
        dataElementStore.save( dataElementC );

        AttributeValue attributeValueA = new AttributeValue( "SOME VALUE", attribute );
        AttributeValue attributeValueB = new AttributeValue( "SOME VALUE", attribute );
        AttributeValue attributeValueC = new AttributeValue( "ANOTHER VALUE", attribute );

        attributeService.addAttributeValue( dataElementA, attributeValueA );
        attributeService.addAttributeValue( dataElementB, attributeValueB );
        attributeService.addAttributeValue( dataElementC, attributeValueC );

        dataElementStore.update( dataElementA );
        dataElementStore.update( dataElementB );
        dataElementStore.update( dataElementC );

        List<AttributeValue> values = dataElementStore.getAttributeValueByAttribute( attribute );
        assertEquals( 3, values.size() );
    }

    @Test
    public void testAttributeValueFromAttributeAndValue() throws NonUniqueAttributeValueException
    {
        Attribute attribute = new Attribute( "test", ValueType.TEXT );
        attribute.setDataElementAttribute( true );
        attributeService.addAttribute( attribute );

        DataElement dataElementA = createDataElement( 'A' );
        DataElement dataElementB = createDataElement( 'B' );
        DataElement dataElementC = createDataElement( 'C' );

        dataElementStore.save( dataElementA );
        dataElementStore.save( dataElementB );
        dataElementStore.save( dataElementC );

        AttributeValue attributeValueA = new AttributeValue( "SOME VALUE", attribute );
        AttributeValue attributeValueB = new AttributeValue( "SOME VALUE", attribute );
        AttributeValue attributeValueC = new AttributeValue( "ANOTHER VALUE", attribute );

        attributeService.addAttributeValue( dataElementA, attributeValueA );
        attributeService.addAttributeValue( dataElementB, attributeValueB );
        attributeService.addAttributeValue( dataElementC, attributeValueC );

        dataElementStore.update( dataElementA );
        dataElementStore.update( dataElementB );
        dataElementStore.update( dataElementC );

        List<AttributeValue> values = dataElementStore.getAttributeValueByAttributeAndValue( attribute, "SOME VALUE" );
        assertEquals( 2, values.size() );

        values = dataElementStore.getAttributeValueByAttributeAndValue( attribute, "ANOTHER VALUE" );
        assertEquals( 1, values.size() );
    }

    @Test
    public void testDataElementByAttributeValue() throws NonUniqueAttributeValueException
    {
        Attribute attribute = new Attribute( "cid", ValueType.TEXT );
        attribute.setDataElementAttribute( true );
        attribute.setUnique( true );
        attributeService.addAttribute( attribute );

        DataElement dataElementA = createDataElement( 'A' );
        DataElement dataElementB = createDataElement( 'B' );
        DataElement dataElementC = createDataElement( 'C' );

        dataElementStore.save( dataElementA );
        dataElementStore.save( dataElementB );
        dataElementStore.save( dataElementC );

        AttributeValue attributeValueA = new AttributeValue( "CID1", attribute );
        AttributeValue attributeValueB = new AttributeValue( "CID2", attribute );
        AttributeValue attributeValueC = new AttributeValue( "CID3", attribute );

        attributeService.addAttributeValue( dataElementA, attributeValueA );
        attributeService.addAttributeValue( dataElementB, attributeValueB );
        attributeService.addAttributeValue( dataElementC, attributeValueC );

        dataElementStore.update( dataElementA );
        dataElementStore.update( dataElementB );
        dataElementStore.update( dataElementC );

        assertNotNull( dataElementStore.getByAttributeValue( attribute, "CID1" ) );
        assertNotNull( dataElementStore.getByAttributeValue( attribute, "CID2" ) );
        assertNotNull( dataElementStore.getByAttributeValue( attribute, "CID3" ) );
        assertNull( dataElementStore.getByAttributeValue( attribute, "CID4" ) );
        assertNull( dataElementStore.getByAttributeValue( attribute, "CID5" ) );

        assertEquals( "DataElementA", dataElementStore.getByAttributeValue( attribute, "CID1" ).getName() );
        assertEquals( "DataElementB", dataElementStore.getByAttributeValue( attribute, "CID2" ).getName() );
        assertEquals( "DataElementC", dataElementStore.getByAttributeValue( attribute, "CID3" ).getName() );
    }
}
