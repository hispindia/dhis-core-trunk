package org.hisp.dhis.system.filter;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.hisp.dhis.DhisConvenienceTest;
import org.hisp.dhis.dataelement.DataElement;
import org.junit.Test;
import org.springframework.test.annotation.NotTransactional;

import static junit.framework.Assert.*;

/**
 * @author Lars Helge Overland
 * @version $Id$
 */
public class AggregateableDataElementPredicateTest
    extends DhisConvenienceTest
{
    @Test
    @NotTransactional
    public void testPredicate()
    {
        DataElement elementA = createDataElement( 'A' );
        DataElement elementB = createDataElement( 'B' );
        DataElement elementC = createDataElement( 'C' );
        DataElement elementD = createDataElement( 'D' );
        DataElement elementE = createDataElement( 'E' );
        DataElement elementF = createDataElement( 'F' );
        
        elementA.setType( DataElement.VALUE_TYPE_BOOL );
        elementB.setType( DataElement.VALUE_TYPE_INT );
        elementC.setType( DataElement.VALUE_TYPE_STRING );
        elementD.setType( DataElement.VALUE_TYPE_BOOL );
        elementE.setType( DataElement.VALUE_TYPE_INT );
        elementF.setType( DataElement.VALUE_TYPE_STRING );        
        
        Set<DataElement> set = new HashSet<DataElement>();
        
        set.add( elementA );
        set.add( elementB );
        set.add( elementC );
        set.add( elementD );
        set.add( elementE );
        set.add( elementF );
        
        Set<DataElement> reference = new HashSet<DataElement>();
        
        reference.add( elementA );
        reference.add( elementB );
        reference.add( elementD );
        reference.add( elementE );
        
        CollectionUtils.filter( set, new AggregateableDataElementPredicate() );
        
        assertEquals( reference.size(), set.size() );
        assertEquals( reference, set );
    }
}
