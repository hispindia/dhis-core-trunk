package org.hisp.dhis.dbmanager.mysql;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hisp.dhis.dbmanager.DataBaseManagerInterface;
import org.hisp.dhis.linelisting.LineListDataValue;
import org.hisp.dhis.linelisting.LineListElement;
import org.hisp.dhis.linelisting.LineListService;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.period.PeriodService;
import org.hisp.dhis.period.comparator.PeriodComparator;
import org.hisp.dhis.source.Source;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

public class MySQLDataBaseManager
    implements DataBaseManagerInterface
{

    private static final Log log = LogFactory.getLog( MySQLDataBaseManager.class );

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private JdbcTemplate jdbcTemplate;

    public void setJdbcTemplate( JdbcTemplate jdbcTemplate )
    {
        this.jdbcTemplate = jdbcTemplate;
    }

    private LineListService lineListService;

    public void setLineListService( LineListService lineListService )
    {
        this.lineListService = lineListService;
    }

    private PeriodService periodService;

    public void setPeriodService( PeriodService periodService )
    {
        this.periodService = periodService;
    }

    private OrganisationUnitService organisationUnitService;

    public void setOrganisationUnitService( OrganisationUnitService organisationUnitService )
    {
        this.organisationUnitService = organisationUnitService;
    }

    // -------------------------------------------------------------------------
    // Create Table
    // -------------------------------------------------------------------------
    public boolean createTable( String tableName, List<String> columnNames, List<String> autoIncrement,
        List<String> dataTypes, List<Integer> sizeOfColumns )
    {
        boolean tableCreated = true;

        PreparedStatement preparedStatement = null;

        String columnDefinition = "CREATE TABLE " + tableName + " ( ";

        for ( int i = 0; i < columnNames.size(); i++ )
        {
            if ( dataTypes.get( i ).equalsIgnoreCase( "DATE" ) || dataTypes.get( i ).equalsIgnoreCase( "text" ) )
            {
                if ( i < ( columnNames.size() - 1 ) )
                {
                    columnDefinition += columnNames.get( i ) + " " + dataTypes.get( i ) + ",";
                } 
                else
                {
                    columnDefinition += columnNames.get( i ) + " " + dataTypes.get( i );
                }
            } 
            else
            {
                if ( i < ( columnNames.size() - 1 ) )
                {
                    columnDefinition += columnNames.get( i ) + " " + dataTypes.get( i ) + "(" + sizeOfColumns.get( i ) + ") " + autoIncrement.get( i ) + ",";
                } 
                else
                {
                    columnDefinition += columnNames.get( i ) + " " + dataTypes.get( i ) + "(" + sizeOfColumns.get( i ) + ") " + autoIncrement.get( i );
                }
            }
        }

        columnDefinition += ");";

        try
        {
            Connection connection = jdbcTemplate.getDataSource().getConnection();

            preparedStatement = connection.prepareStatement( columnDefinition );

            preparedStatement.execute();

            preparedStatement.close();
        } 
        catch ( SQLException e )
        {
            tableCreated = false;

            e.printStackTrace();
        }

        return tableCreated;
    }

    // -------------------------------------------------------------------------
    // Drop Table
    // -------------------------------------------------------------------------

    public void dropTable( String tableName )
    {
        PreparedStatement preparedStatement = null;
        
        try
        {
            Connection connection = jdbcTemplate.getDataSource().getConnection();

            String columnDefinition = "";

            columnDefinition += "DROP TABLE " + tableName + " ;";

            preparedStatement = connection.prepareStatement( columnDefinition );

            preparedStatement.execute();
        } 
        catch ( SQLException e )
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if( preparedStatement != null ) preparedStatement.close();
            }
            catch( Exception e )
            {
                
            }
        }
    }

    // -------------------------------------------------------------------------
    // Check if any data exists in Table
    // -------------------------------------------------------------------------

    public boolean checkDataFromTable( String tableName, LineListElement lineListElement )
    {
        boolean doNotDelete = false;
        int recordCount = 0;
        try
        {
            String columnDefinition = "SELECT COUNT(" + lineListElement.getShortName() + ") FROM " + tableName;
            SqlRowSet rs = jdbcTemplate.queryForRowSet( columnDefinition );
            
            if( rs != null && rs.next() )
            {
                recordCount = rs.getInt( 1 );                
            }
            
            if( recordCount > 0 )
            {
                doNotDelete = true;
            }

            log.debug( tableName + ", " + lineListElement.getShortName() + (doNotDelete ? " has data" : " can be deleted") );
        } 
        catch ( Exception e )
        {
            log.error( "Caught exception while checking " + tableName + ", " + lineListElement.getShortName() + ". Won't delete.", e );
            doNotDelete = false;
        }

        return doNotDelete;
    }
    
    // -------------------------------------------------------------------------
    // Get Max Record Number from Department table
    // -------------------------------------------------------------------------
    public int getMaxRecordNumber( String tableName )
    {
        int maxRecordNumber = 0;
        try
        {
            String query = "SELECT MAX(recordNumber) FROM " + tableName;
            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );
           
            if( rs.next() )
            {
                maxRecordNumber = rs.getInt( 1 );
            }
        } 
        catch ( Exception e )
        {
            e.printStackTrace();
        }
        return maxRecordNumber;
    }
    
// function for row count
    public int rowCount( String tableName )
    {
        int noOfRows = 0;
        try
        {
           //String rowCount = null;
            //Connection connection = jdbcTemplate.getDataSource().getConnection();
            String query = "SELECT COUNT(*) FROM " + tableName;
           // String query = "select * from " + tableName + " ;";
            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );
           
            if( rs.next() )
            {
                noOfRows = rs.getInt( 1 );
            }
        } 
        catch ( Exception e )
        {
            e.printStackTrace();
        }
        return noOfRows;
    }
    
    public boolean updateTable( String tableName, List<LineListElement> removeList, List<LineListElement> addList )
    {
        String columnDefinition = "";

        Statement statement = null;

        columnDefinition += "ALTER TABLE " + tableName;

        int columnAffected = 0;
        boolean rowUpdated = false;
        boolean columnAdded = false;
        if ( addList != null && !addList.isEmpty() )
        {
            int i = 1;
            Iterator<LineListElement> addListItr = addList.iterator();
            int size = addList.size();
            while ( addListItr.hasNext() )
            {
                LineListElement lineListElement = (LineListElement) addListItr.next();
                if ( lineListElement.getDataType().equalsIgnoreCase( "string" ) )
                {
                    columnDefinition += " ADD COLUMN " + lineListElement.getShortName() + " VARCHAR (255)";
                    columnAdded = true;
                } 
                else if ( lineListElement.getDataType().equalsIgnoreCase( "bool" ) )
                {
                    columnDefinition += " ADD COLUMN " + lineListElement.getShortName() + "BIT (1)";
                    columnAdded = true;
                } 
                else if ( lineListElement.getDataType().equalsIgnoreCase( "date" ) )
                {
                    columnDefinition += " ADD COLUMN " + lineListElement.getShortName() + " DATE";
                    columnAdded = true;
                } 
                else if ( lineListElement.getDataType().equalsIgnoreCase( "int" ) )
                {
                    columnDefinition += " ADD COLUMN " + lineListElement.getShortName() + " int (11)";
                    columnAdded = true;
                } 

                if ( i < size )
                {
                    columnDefinition += " ,";
                    i++;
                }
            }
        }

        System.out.println( removeList.size() );
        if ( removeList != null )
        {
            if ( !( removeList.isEmpty() ) )
            {

                int j = 1;
                Iterator<LineListElement> removeListItr = removeList.iterator();
                int size = removeList.size();

                while ( removeListItr.hasNext() )
                {
                    LineListElement element = (LineListElement) removeListItr.next();
                    if ( columnAdded )
                    {
                        columnDefinition += " , drop column " + element.getShortName();
                        columnAdded = false;
                    } else
                    {
                        columnDefinition += " drop column " + element.getShortName();
                    }
                    System.out.println( " element = " + element.getShortName() );
                    if ( j < size )
                    {
                        columnDefinition += " ,";
                        j++;
                    }
                }

                System.out.println( " columnDefinition = " + columnDefinition );
            }
        }
        System.out.println( columnDefinition );

        try
        {
            Connection connection = jdbcTemplate.getDataSource().getConnection();

            statement = connection.createStatement();

            columnAffected = statement.executeUpdate( columnDefinition );

            System.out.println( columnAffected );
            rowUpdated = true;
            statement.close();

        } catch ( SQLException e )
        {

            rowUpdated = false;
            e.printStackTrace();
        }

        return rowUpdated;
    }

    public List<LineListDataValue> getFromLLTable( String tableName, Source source, Period period )
    {
        String columnDefinition = "";
        //Statement statement = null;

        // creating map of element and its values
        Map<String, String> llElementValuesMap = new HashMap<String, String>();

        List<LineListDataValue> llDataValues = new ArrayList<LineListDataValue>();
        // LineListDataValue llDataValue = new LineListDataValue();
        if ( period != null && source != null )
        {
            columnDefinition += "select * from " + tableName + " where periodid = " + period.getId() + " and sourceid = " + source.getId() + " order by recordnumber";

            Collection<LineListElement> elementsCollection = new ArrayList<LineListElement>();

            elementsCollection = lineListService.getLineListGroupByShortName( tableName ).getLineListElements();

            LineListElement element;
            String name = "";

            try
            {
                //Connection connection = jdbcTemplate.getDataSource().getConnection();
                //statement = connection.createStatement( ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE );
                //ResultSet result = statement.executeQuery( columnDefinition );
                SqlRowSet result = jdbcTemplate.queryForRowSet( columnDefinition );
                System.out.println( columnDefinition );

                if ( result != null )
                {
                    result.beforeFirst();

                    while ( result.next() )
                    {
                        LineListDataValue llDataValue = new LineListDataValue();
                        llDataValue.setRecordNumber( result.getInt( "recordnumber" ) );
                        Iterator it1 = elementsCollection.iterator();
                        while ( it1.hasNext() )
                        {
                            element = (LineListElement) it1.next();
                            name = element.getShortName() + ":" + result.getInt( "recordnumber" );
                            if ( element.getDataType().equalsIgnoreCase( "string" ) )
                            {
                                llElementValuesMap.put( name, result.getString( element.getShortName() ) );
                            } else
                            {
                                if ( element.getDataType().equalsIgnoreCase( "date" ) )
                                {
                                    llElementValuesMap.put( name, result.getDate( element.getShortName() ).toString() );
                                } else
                                {
                                    if ( element.getDataType().equalsIgnoreCase( "int" ) )
                                    {
                                        llElementValuesMap.put( name, Integer.toString( result.getInt( element.getShortName() ) ) );
                                    }
                                }
                            }
                            //System.out.println("Key = "+name+ "Value = "+llElementValuesMap.get(name));
                        }

                        llDataValue.setLineListValues( llElementValuesMap );
                        llDataValue.setPeriod( period );
                        llDataValue.setSource( source );
                        llDataValues.add( llDataValue );
                    }

                }
                //statement.close();

            } catch ( Exception e )
            {
                e.printStackTrace();
            }
        }
        return llDataValues;
    }

    public List<LineListDataValue> getLLValuesByLLElementValue( String tableName, String llElementName, String llElementValue, Source source, Period period )
    {
        String columnDefinition = "";
        //Statement statement = null;

        // creating map of element and its values
        Map<String, String> llElementValuesMap = new HashMap<String, String>();

        List<LineListDataValue> llDataValues = new ArrayList<LineListDataValue>();
        // LineListDataValue llDataValue = new LineListDataValue();
        if ( period != null && source != null )
        {
            columnDefinition += "select * from " + tableName + " where periodid = " + period.getId() + " and sourceid = " + source.getId() + " and " + llElementName + " LIKE '" + llElementValue + "' order by recordnumber";

            System.out.println( columnDefinition );

            Collection<LineListElement> elementsCollection = new ArrayList<LineListElement>();

            elementsCollection = lineListService.getLineListGroupByShortName( tableName ).getLineListElements();

            LineListElement element;
            String name = "";

            try
            {
               //Connection connection = jdbcTemplate.getDataSource().getConnection();
               // statement = connection.createStatement( ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE );
                //ResultSet result = statement.executeQuery( columnDefinition );
                 SqlRowSet result = jdbcTemplate.queryForRowSet( columnDefinition );

                if ( result != null )
                {
                    result.beforeFirst();

                    while ( result.next() )
                    {
                        LineListDataValue llDataValue = new LineListDataValue();
                        llDataValue.setRecordNumber( result.getInt( "recordnumber" ) );
                        Iterator it1 = elementsCollection.iterator();
                        while ( it1.hasNext() )
                        {
                            element = (LineListElement) it1.next();
                            name = element.getShortName() + ":" + result.getInt( "recordnumber" );
                            if ( element.getDataType().equalsIgnoreCase( "string" ) )
                            {
                                String tempString = result.getString( element.getShortName() );
                                if ( tempString == null )
                                {
                                    tempString = "";
                                }
                                llElementValuesMap.put( name, tempString );
                            } else
                            {
                                if ( element.getDataType().equalsIgnoreCase( "date" ) )
                                {
                                    Date tempDate = result.getDate( element.getShortName() );
                                    String tempStr = "";
                                    if ( tempDate != null )
                                    {
                                        tempStr = tempDate.toString();
                                    }
                                    llElementValuesMap.put( name, tempStr );
                                } else
                                {
                                    if ( element.getDataType().equalsIgnoreCase( "int" ) )
                                    {
                                        String tempStr = "";
                                        Integer tempInt = result.getInt( element.getShortName() );
                                        if ( tempInt != null )
                                        {
                                            tempStr = Integer.toString( tempInt );
                                        }
                                        llElementValuesMap.put( name, tempStr );
                                    }
                                }
                            }
                        }

                        llDataValue.setLineListValues( llElementValuesMap );
                        llDataValue.setPeriod( period );
                        llDataValue.setSource( source );
                        llDataValues.add( llDataValue );
                    }

                }
                //statement.close();

            } catch ( Exception e )
            {
                e.printStackTrace();
            }
        }
        return llDataValues;

    }

    public List<LineListDataValue> getLLValuesFilterByLLElements( String tableName, Map<String, String> llElementValueMap, Source source, Period period )
    {
        String columnDefinition = "";

        //Statement statement = null;

        // creating map of element and its values
        //Map<String, String> llElementValuesMap = new HashMap<String, String>();

        List<LineListDataValue> llDataValues = new ArrayList<LineListDataValue>();
        // LineListDataValue llDataValue = new LineListDataValue();
        if ( period != null && source != null )
        {
            columnDefinition += "select * from " + tableName + " where periodid = " + period.getId() + " and sourceid = " + source.getId();

            List<String> llElementNames = new ArrayList<String>( llElementValueMap.keySet() );
            Iterator<String> llENamesIterator = llElementNames.iterator();
            while ( llENamesIterator.hasNext() )
            {
                String lleName = llENamesIterator.next();

                String lleValue = llElementValueMap.get( lleName );

                if ( lleValue.equalsIgnoreCase( "notnull" ) )
                {
                    columnDefinition += " and " + lleName + " IS NOT NULL";
                } else
                {
                    if ( lleValue.equalsIgnoreCase( "null" ) )
                    {
                        columnDefinition += " and " + lleName + " IS NULL";
                    } else
                    {
                        columnDefinition += " and " + lleName + " LIKE '" + lleValue + "'";
                    }
                }
            }


            columnDefinition += " order by recordnumber";

            System.out.println( columnDefinition );

            Collection<LineListElement> elementsCollection = new ArrayList<LineListElement>();

            elementsCollection = lineListService.getLineListGroupByShortName( tableName ).getLineListElements();

            LineListElement element;
            String name = "";

            try
            {
               //Connection connection = jdbcTemplate.getDataSource().getConnection();
                //statement = connection.createStatement( ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE );
                //ResultSet result = statement.executeQuery( columnDefinition );
                SqlRowSet result = jdbcTemplate.queryForRowSet( columnDefinition );
                //System.out.println(columnDefinition);

                if ( result != null )
                {
                    result.beforeFirst();

                    while ( result.next() )
                    {
                        LineListDataValue llDataValue = new LineListDataValue();
                        Map<String, String> llElementValuesMap = new HashMap<String, String>();
                        llDataValue.setRecordNumber( result.getInt( "recordnumber" ) );
                        Iterator it1 = elementsCollection.iterator();
                        while ( it1.hasNext() )
                        {
                            element = (LineListElement) it1.next();
                            //name = element.getShortName() + ":" + result.getInt( "recordnumber" );
                            name = element.getShortName();
                            if ( element.getDataType().equalsIgnoreCase( "string" ) )
                            {
                                String tempString = result.getString( element.getShortName() );
                                if ( tempString == null )
                                {
                                    tempString = "";
                                }
                                llElementValuesMap.put( name, tempString );
                            } else
                            {
                                if ( element.getDataType().equalsIgnoreCase( "date" ) )
                                {
                                    Date tempDate = result.getDate( element.getShortName() );
                                    String tempStr = "";
                                    if ( tempDate != null )
                                    {
                                        tempStr = tempDate.toString();
                                    }
                                    llElementValuesMap.put( name, tempStr );
                                } else
                                {
                                    if ( element.getDataType().equalsIgnoreCase( "int" ) )
                                    {
                                        String tempStr = "";
                                        Integer tempInt = result.getInt( element.getShortName() );
                                        if ( tempInt != null )
                                        {
                                            tempStr = Integer.toString( tempInt );
                                        }
                                        llElementValuesMap.put( name, tempStr );
                                    }
                                }
                            }
                        }

                        llDataValue.setLineListValues( llElementValuesMap );
                        llDataValue.setPeriod( period );
                        llDataValue.setSource( source );
                        llDataValues.add( llDataValue );
                    }

                }

            } catch ( Exception e )
            {
                e.printStackTrace();
            }
        }
        return llDataValues;

    }
    
    public List<LineListDataValue> getLLValuesFilterByLLElements( String tableName, Map<String, String> llElementValueMap, Source source )
    {
        String columnDefinition = "";

        //Statement statement = null;

        // creating map of element and its values
        //Map<String, String> llElementValuesMap = new HashMap<String, String>();

        List<LineListDataValue> llDataValues = new ArrayList<LineListDataValue>();
        // LineListDataValue llDataValue = new LineListDataValue();
        if ( source != null )
        {
            columnDefinition += "select * from " + tableName + " where sourceid = " + source.getId();

            List<String> llElementNames = new ArrayList<String>( llElementValueMap.keySet() );
            Iterator<String> llENamesIterator = llElementNames.iterator();
            while ( llENamesIterator.hasNext() )
            {
                String lleName = llENamesIterator.next();

                String lleValue = llElementValueMap.get( lleName );

                if ( lleValue.equalsIgnoreCase( "notnull" ) )
                {
                    columnDefinition += " and " + lleName + " IS NOT NULL";
                } else
                {
                    if ( lleValue.equalsIgnoreCase( "null" ) )
                    {
                        columnDefinition += " and " + lleName + " IS NULL";
                    } else
                    {
                        columnDefinition += " and " + lleName + " LIKE '" + lleValue + "'";
                    }
                }
            }


            columnDefinition += " order by recordnumber";

            System.out.println( columnDefinition );

            Collection<LineListElement> elementsCollection = new ArrayList<LineListElement>();

            elementsCollection = lineListService.getLineListGroupByShortName( tableName ).getLineListElements();

            LineListElement element;
            String name = "";

            try
            {
               //Connection connection = jdbcTemplate.getDataSource().getConnection();
                //statement = connection.createStatement( ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE );
                //ResultSet result = statement.executeQuery( columnDefinition );
                SqlRowSet result = jdbcTemplate.queryForRowSet( columnDefinition );
                //System.out.println(columnDefinition);

                if ( result != null )
                {
                    result.beforeFirst();

                    while ( result.next() )
                    {
                        LineListDataValue llDataValue = new LineListDataValue();
                        Map<String, String> llElementValuesMap = new HashMap<String, String>();
                        llDataValue.setRecordNumber( result.getInt( "recordnumber" ) );
                        Iterator it1 = elementsCollection.iterator();
                        while ( it1.hasNext() )
                        {
                            element = (LineListElement) it1.next();
                            //name = element.getShortName() + ":" + result.getInt( "recordnumber" );
                            name = element.getShortName();
                            if ( element.getDataType().equalsIgnoreCase( "string" ) )
                            {
                                String tempString = result.getString( element.getShortName() );
                                if ( tempString == null )
                                {
                                    tempString = "";
                                }
                                llElementValuesMap.put( name, tempString );
                            } else
                            {
                                if ( element.getDataType().equalsIgnoreCase( "date" ) )
                                {
                                    Date tempDate = result.getDate( element.getShortName() );
                                    String tempStr = "";
                                    if ( tempDate != null )
                                    {
                                        tempStr = tempDate.toString();
                                    }
                                    llElementValuesMap.put( name, tempStr );
                                } else
                                {
                                    if ( element.getDataType().equalsIgnoreCase( "int" ) )
                                    {
                                        String tempStr = "";
                                        Integer tempInt = result.getInt( element.getShortName() );
                                        if ( tempInt != null )
                                        {
                                            tempStr = Integer.toString( tempInt );
                                        }
                                        llElementValuesMap.put( name, tempStr );
                                    }
                                }
                            }
                        }

                        llDataValue.setLineListValues( llElementValuesMap );
                        llDataValue.setSource( source );
                        llDataValues.add( llDataValue );
                    }

                }

            } catch ( Exception e )
            {
                e.printStackTrace();
            }
        }
        return llDataValues;

    }

    public int getLLValueCountByLLElements( String tableName, Map<String, String> llElementValueMap, Source source )
    {
        String columnDefinition = "";
        int noOfRows = 0;
        if ( source != null )
        {
            //columnDefinition += "SELECT COUNT(*) FROM " + tableName + " WHERE periodid = " + period.getId() + " AND sourceid = " + source.getId();
            columnDefinition += "SELECT COUNT(*) FROM " + tableName + " WHERE sourceid = " + source.getId();

            List<String> llElementNames = new ArrayList<String>( llElementValueMap.keySet() );
            Iterator<String> llENamesIterator = llElementNames.iterator();
            while ( llENamesIterator.hasNext() )
            {
                String lleName = llENamesIterator.next();

                String lleValue = llElementValueMap.get( lleName );

                if ( lleValue.equalsIgnoreCase( "notnull" ) )
                {
                    columnDefinition += " AND " + lleName + " IS NOT NULL";
                } else
                {
                    if ( lleValue.equalsIgnoreCase( "null" ) )
                    {
                        columnDefinition += " AND " + lleName + " IS NULL";
                    } else
                    {
                        columnDefinition += " AND " + lleName + " LIKE '" + lleValue + "'";
                    }
                }
            }
            columnDefinition += " order by recordnumber";
            System.out.println( columnDefinition );

            try
            {
                SqlRowSet rs = jdbcTemplate.queryForRowSet( columnDefinition );
               
                if( rs.next() )
                {
                    noOfRows = rs.getInt( 1 );
                }
            } 
            catch ( Exception e )
            {
                e.printStackTrace();
            }
        }
        return noOfRows;
    }

    
    
    
    public List<LineListDataValue> getLLValuesSortBy( String tableName, String sortBy, Source source, Period period )
    {
        String columnDefinition = "";
        //Statement statement = null;

        // creating map of element and its values
        Map<String, String> llElementValuesMap = new HashMap<String, String>();

        List<LineListDataValue> llDataValues = new ArrayList<LineListDataValue>();
        // LineListDataValue llDataValue = new LineListDataValue();
        if ( period != null && source != null )
        {
            columnDefinition += "select * from " + tableName + " where periodid = " + period.getId() + " and sourceid = " + source.getId() + " order by " + sortBy;

            System.out.println( columnDefinition );

            Collection<LineListElement> elementsCollection = new ArrayList<LineListElement>();

            elementsCollection = lineListService.getLineListGroupByShortName( tableName ).getLineListElements();

            LineListElement element;
            String name = "";

            try
            {
                //Connection connection = jdbcTemplate.getDataSource().getConnection();

                //statement = connection.createStatement( ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE );
            	
            	SqlRowSet sqlResultSet = jdbcTemplate.queryForRowSet( columnDefinition );

                //ResultSet result = statement.executeQuery( columnDefinition );
                //System.out.println(columnDefinition);

                if ( sqlResultSet != null )
                {
                	sqlResultSet.beforeFirst();

                    while ( sqlResultSet.next() )
                    {
                        LineListDataValue llDataValue = new LineListDataValue();
                        llDataValue.setRecordNumber( sqlResultSet.getInt( "recordnumber" ) );
                        
                        Iterator<LineListElement> it1 = elementsCollection.iterator();
                        while ( it1.hasNext() )
                        {
                            element = (LineListElement) it1.next();
                            name = element.getShortName() + ":" + sqlResultSet.getInt( "recordnumber" );
                            
                            if ( element.getDataType().equalsIgnoreCase( "string" ) )
                            {
                                String tempString = sqlResultSet.getString( element.getShortName() );
                                if ( tempString == null )
                                {
                                    tempString = "";
                                }
                                llElementValuesMap.put( name, tempString );
                            } 
                            else if ( element.getDataType().equalsIgnoreCase( "date" ) )
                            {
                                Date tempDate = sqlResultSet.getDate( element.getShortName() );
                                String tempStr = "";
                                if ( tempDate != null )
                                {
                                    tempStr = tempDate.toString();
                                }
                                llElementValuesMap.put( name, tempStr );
                            } 
                            else if ( element.getDataType().equalsIgnoreCase( "int" ) )
                            {
                                String tempStr = "";
                                Integer tempInt = sqlResultSet.getInt( element.getShortName() );
                                if ( tempInt != null )
                                {
                                    tempStr = Integer.toString( tempInt );
                                }
                                llElementValuesMap.put( name, tempStr );
                            }                                
                        }

                        llDataValue.setLineListValues( llElementValuesMap );
                        llDataValue.setPeriod( period );
                        llDataValue.setSource( source );
                        llDataValues.add( llDataValue );
                    }

                }// while loop end
                //result.close();
                //statement.close();
            }// Try block end 
            catch ( Exception e )
            {
                e.printStackTrace();
            }
        }// if block end
        return llDataValues;
    }

    
    public boolean insertSingleLLValueIntoDb( LineListDataValue llDataValue, String tableName )
    {
        boolean updateLLValue = false;

        String columnDefinition = "";
        
        columnDefinition = "INSERT INTO " + tableName + " (periodid,sourceid,storedby,lastupdated,";
        
        Period period = llDataValue.getPeriod();

        Source source = llDataValue.getSource();

        Map<String, String> elementValues = llDataValue.getLineListValues();
        Set<String> elements = elementValues.keySet();

        int size = elements.size();
        int i = 1;
        java.util.Date today = llDataValue.getTimestamp();
        long t;
        if ( today == null )
        {
            Date d = new Date();
            t = d.getTime();
        } 
        else
        {
            t = today.getTime();
        }

        java.sql.Date date = new java.sql.Date( t );
        String values = " values (" + period.getId() + "," + source.getId() + ",'" + llDataValue.getStoredBy() + "','" + date + "',";
        for ( String elementName : elements )
        {
            LineListElement lineListElement = lineListService.getLineListElementByShortName( elementName );
            if ( i == size )
            {
                columnDefinition += elementName + ")";

                if ( lineListElement.getDataType().equalsIgnoreCase( "int" ) )
                {
                    values += Integer.parseInt( elementValues.get( elementName ) );
                } 
                else
                {
                    values += "'" + elementValues.get( elementName ) + "'";
                }
            } 
            else
            {
                columnDefinition += elementName + ",";
                if( lineListElement.getDataType().equalsIgnoreCase( "int" ) )
                {
                    values += Integer.parseInt( elementValues.get( elementName ) ) + ",";
                } 
                else
                {
                    values += "'" + elementValues.get( elementName ) + "'" + ",";
                }
                i++;
            }

        }
        columnDefinition += values + ")";
        
        System.out.println("ColimnDefination in LLSingleDatavalue " + columnDefinition );

        try
        {
            int sqlResult = jdbcTemplate.update( columnDefinition );            
            updateLLValue = true;
            columnDefinition = "";
        } 
        catch ( Exception e )
        {
            e.printStackTrace();
            updateLLValue = false;
        }        
        return updateLLValue;
    }

    
    public boolean insertLLValueIntoDb( List<LineListDataValue> llDataValuesList, String tableName )
    {
        boolean updateLLValue = false;

        String columnDefinition = "";

        for ( LineListDataValue llDataValue : llDataValuesList )
        {

            columnDefinition = "insert into " + tableName + " (periodid,sourceid,storedby,lastupdated,";
            // get periodid from llDataValue

            Period period = llDataValue.getPeriod();

            Source source = llDataValue.getSource();

            // System.out.println(period.getId() + " " + source.getId());

            Map<String, String> elementValues = llDataValue.getLineListValues();
            Set<String> elements = elementValues.keySet();

            int size = elements.size();
            int i = 1;
            java.util.Date today = llDataValue.getTimestamp();
            long t;
            if ( today == null )
            {
                Date d = new Date();
                t = d.getTime();
            } else
            {
                t = today.getTime();
            }

            java.sql.Date date = new java.sql.Date( t );
            String values = " values (" + period.getId() + "," + source.getId() + ",'" + llDataValue.getStoredBy() + "','" + date + "',";
            for ( String elementName : elements )
            {

                // String name = elementName.replaceAll(" ", "_");
                LineListElement lineListElement = lineListService.getLineListElementByShortName( elementName );
                if ( i == size )
                {
                    columnDefinition += elementName + ")";

                    if ( lineListElement.getDataType().equalsIgnoreCase( "int" ) )
                    {
                        values += Integer.parseInt( elementValues.get( elementName ) );
                    } 
                    else
                    {
                        values += "'" + elementValues.get( elementName ) + "'";
                    }
                } else
                {
                    columnDefinition += elementName + ",";
                    if ( lineListElement.getDataType().equalsIgnoreCase( "int" ) )
                    {
                        values += Integer.parseInt( elementValues.get( elementName ) ) + ",";
                    } else
                    {
                        values += "'" + elementValues.get( elementName ) + "'" + ",";
                    }
                    i++;
                }

            }
            columnDefinition += values + ")";
            System.out.println( "Column Definition = " + columnDefinition );

            try
            {
                //Connection connection = jdbcTemplate.getDataSource().getConnection();
                //preparedStatement = connection.prepareStatement( columnDefinition );
                //updateLLValue = preparedStatement.execute();
                int sqlResult = jdbcTemplate.update( columnDefinition );
                updateLLValue = true;
                columnDefinition = "";
            } catch ( Exception e )
            {
                e.printStackTrace();
                updateLLValue = false;
            }

        }

        return updateLLValue;

    }

    public boolean removeLLRecord( int recordId, String tableName )
    {
        boolean valueDeleted = false;

        String columnDefinition = " delete from " + tableName + " where recordnumber = " + recordId;

        PreparedStatement preparedStatement = null;

        try
        {
            //Connection connection = jdbcTemplate.getDataSource().getConnection();
            //preparedStatement = connection.prepareStatement( columnDefinition );
           // valueDeleted = preparedStatement.execute();
            int sqlResult = jdbcTemplate.update( columnDefinition );
            valueDeleted = true;
            columnDefinition = "";
            //preparedStatement.close();
        } catch ( Exception e )
        {
            e.printStackTrace();
            valueDeleted = false;
        }

        return valueDeleted;
    }

    public boolean updateLLValue( List<LineListDataValue> llDataValuesList, String tableName )
    {
        boolean valueUpdated = false;

        String columnDefinition = "";

        PreparedStatement preparedStatement = null;

        for ( LineListDataValue llDataValue : llDataValuesList )
        {

            // UPDATE table_name SET column1=value, column2=value2,... WHERE
            // some_column=some_value
            columnDefinition = "update " + tableName + " set ";
            // get periodid from llDataValue

            Map<String, String> elementValues = llDataValue.getLineListValues();
            // System.out.println("size of map = "+elementValues.size());
            Set<String> elements = elementValues.keySet();
            System.out.println("In Update recordnumber = " + llDataValue.getRecordNumber());
            int size = elements.size();
            //System.out.println("size = "+size);
            int i = 1;
            java.util.Date today = llDataValue.getTimestamp();
            long t = today.getTime();
            java.sql.Date date = new java.sql.Date( t );
            String whereClause = " where recordnumber = " + llDataValue.getRecordNumber();
            for ( String elementName : elements )
            {
                //System.out.println( "elementName = " + elementName );
                LineListElement lineListElement = lineListService.getLineListElementByShortName( elementName );
                if ( i == size )
                {

                    if ( lineListElement.getDataType().equalsIgnoreCase( "int" ) )
                    {
                        try
                        {
                            columnDefinition += elementName + " = " + Integer.parseInt( elementValues.get( elementName ) ) + ",";
                            llDataValue.getSource().getId();
                            columnDefinition += "periodid = '" + llDataValue.getPeriod().getId() + "', sourceid = '" + llDataValue.getSource().getId() + "', storedby = '" + llDataValue.getStoredBy() + "', lastupdated = '" + date + "' ";

                        } catch ( Exception e )
                        {
                            JOptionPane.showMessageDialog( null, elementName + " cannot be null" );
                        }
                    } else
                    {
                        columnDefinition += elementName + " = '" + elementValues.get( elementName ) + "'" + ",";
                        columnDefinition += "periodid = '" + llDataValue.getPeriod().getId() + "', sourceid = '" + llDataValue.getSource().getId() + "', storedby = '" + llDataValue.getStoredBy() + "', lastupdated = '" + date + "' ";
                    }
                } else
                {
                    if ( lineListElement.getDataType().equalsIgnoreCase( "int" ) )
                    {
                        try
                        {
                            columnDefinition += elementName + " = " + Integer.parseInt( elementValues.get( elementName ) ) + ",";
                        } catch ( Exception e )
                        {
                            JOptionPane.showMessageDialog( null, elementName + " cannot be null " );
                        }
                    } else
                    {
                        columnDefinition += elementName + " = '" + elementValues.get( elementName ) + "'" + ",";
                    }
                    i++;
                }

            }

            columnDefinition += whereClause;
            System.out.println("Update Definition = " + columnDefinition);

            try
            {
                //Connection connection = jdbcTemplate.getDataSource().getConnection();

                //preparedStatement = connection.prepareStatement( columnDefinition );
                int sqlResult = jdbcTemplate.update( columnDefinition );
                valueUpdated = true;
                //valueUpdated = preparedStatement.execute();
                columnDefinition = "";
                //preparedStatement.close();
            } catch ( Exception e )
            {
                e.printStackTrace();
                valueUpdated = false;
            }

        }

        return valueUpdated;
    }

    
    public boolean updateSingleLLValue( LineListDataValue llDataValue, String tableName )
    {
        boolean valueUpdated = false;

        String columnDefinition = "";

        columnDefinition = "UPDATE " + tableName + " SET ";

        Map<String, String> elementValues = llDataValue.getLineListValues();

        Set<String> elements = elementValues.keySet();
        System.out.println("In Update recordnumber = " + llDataValue.getRecordNumber());
        int size = elements.size();
        int i = 1;
        java.util.Date today = llDataValue.getTimestamp();
        long t = today.getTime();
        java.sql.Date date = new java.sql.Date( t );
        String whereClause = " WHERE recordnumber = " + llDataValue.getRecordNumber();
        for ( String elementName : elements )
        {
            LineListElement lineListElement = lineListService.getLineListElementByShortName( elementName );
            if ( i == size )
            {
                if ( lineListElement.getDataType().equalsIgnoreCase( "int" ) )
                {
                    try
                    {
                        columnDefinition += elementName + " = " + Integer.parseInt( elementValues.get( elementName ) ) + ",";
                        llDataValue.getSource().getId();
                        columnDefinition += "periodid = '" + llDataValue.getPeriod().getId() + "', sourceid = '" + llDataValue.getSource().getId() + "', storedby = '" + llDataValue.getStoredBy() + "', lastupdated = '" + date + "' ";

                    } 
                    catch ( Exception e )
                    {
                        System.out.println( "Exception: "+ e.getMessage() );
                    }
                } 
                else
                {
                    columnDefinition += elementName + " = '" + elementValues.get( elementName ) + "'" + ",";
                    columnDefinition += "periodid = '" + llDataValue.getPeriod().getId() + "', sourceid = '" + llDataValue.getSource().getId() + "', storedby = '" + llDataValue.getStoredBy() + "', lastupdated = '" + date + "' ";
                }
            } 
            else
            {
                if ( lineListElement.getDataType().equalsIgnoreCase( "int" ) )
                {
                    try
                    {
                        columnDefinition += elementName + " = " + Integer.parseInt( elementValues.get( elementName ) ) + ",";
                    } 
                    catch ( Exception e )
                    {
                        System.out.println( "Exception: "+ e.getMessage() );
                    }
                } else
                {
                    columnDefinition += elementName + " = '" + elementValues.get( elementName ) + "'" + ",";
                }
                i++;
            }

        }

        columnDefinition += whereClause;
        System.out.println("Update Definition = " + columnDefinition);

        try
        {
            int sqlResult = jdbcTemplate.update( columnDefinition );
            valueUpdated = true;
            columnDefinition = "";
        } 
        catch ( Exception e )
        {
            e.printStackTrace();
            valueUpdated = false;
        }

        return valueUpdated;
    }

    
    public Period getRecentPeriodForOnChangeData( String tableName, String llElementName, String llElementValue, Source source )
    {
        String columnDefinition = "";

        //Statement statement = null;

        List<Period> periodList = new ArrayList<Period>();

        if ( source != null )
        {
            columnDefinition += "select distinct(periodid) from " + tableName + " where sourceid = " + source.getId() + " and " + llElementName + " LIKE '" + llElementValue + "' order by recordnumber";

            System.out.println( columnDefinition );

            try
            {
                //Connection connection = jdbcTemplate.getDataSource().getConnection();

                //statement = connection.createStatement( ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE );

                //ResultSet result = statement.executeQuery( columnDefinition );
            	
            	SqlRowSet sqlResultSet = jdbcTemplate.queryForRowSet( columnDefinition );

                if ( sqlResultSet != null )
                {
                	sqlResultSet.beforeFirst();

                    while ( sqlResultSet.next() )
                    {
                        int tempPeriodId = sqlResultSet.getInt( 1 );
                        Period tempPeriod = periodService.getPeriod( tempPeriodId );
                        periodList.add( tempPeriod );
                    }

                }

                //result.close();

                //statement.close();
            } catch ( Exception e )
            {
                e.printStackTrace();
            }
        }

        Collections.sort( periodList, new PeriodComparator() );

        if ( periodList != null && periodList.size() > 0 )
        {
            return periodList.get( 0 );
        } else
        {
            return periodService.getPeriod( 0 );
        }
    }

    public LineListDataValue getLLValuesByRecordNumber( String tableName, int recordId )
    {
        String columnDefinition = "";
        //Statement statement = null;

        // creating map of element and its values
        Map<String, String> llElementValuesMap = new HashMap<String, String>();

        LineListDataValue llDataValue = new LineListDataValue();
        // LineListDataValue llDataValue = new LineListDataValue();

            columnDefinition += "select * from " + tableName + "where recordnumber = "+recordId;

            System.out.println( columnDefinition );

            Collection<LineListElement> elementsCollection = new ArrayList<LineListElement>();

            elementsCollection = lineListService.getLineListGroupByShortName( tableName ).getLineListElements();

            LineListElement element;
            String name = "";

            try
            {
               // Connection connection = jdbcTemplate.getDataSource().getConnection();
                //statement = connection.createStatement( ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE );
                //ResultSet result = statement.executeQuery( columnDefinition );
                SqlRowSet result = jdbcTemplate.queryForRowSet( columnDefinition );
                if ( result != null )
                {
                    result.beforeFirst();

                    while ( result.next() )
                    {
                        llDataValue.setRecordNumber( result.getInt( "recordnumber" ) );
                        Iterator it1 = elementsCollection.iterator();
                        while ( it1.hasNext() )
                        {
                            element = (LineListElement) it1.next();
                            name = element.getShortName() + ":" + result.getInt( "recordnumber" );
                            if ( element.getDataType().equalsIgnoreCase( "string" ) )
                            {
                                String tempString = result.getString( element.getShortName() );
                                if ( tempString == null )
                                {
                                    tempString = "";
                                }
                                llElementValuesMap.put( name, tempString );
                            } else
                            {
                                if ( element.getDataType().equalsIgnoreCase( "date" ) )
                                {
                                    Date tempDate = result.getDate( element.getShortName() );
                                    String tempStr = "";
                                    if ( tempDate != null )
                                    {
                                        tempStr = tempDate.toString();
                                    }
                                    llElementValuesMap.put( name, tempStr );
                                } else
                                {
                                    if ( element.getDataType().equalsIgnoreCase( "int" ) )
                                    {
                                        String tempStr = "";
                                        Integer tempInt = result.getInt( element.getShortName() );
                                        if ( tempInt != null )
                                        {
                                            tempStr = Integer.toString( tempInt );
                                        }
                                        llElementValuesMap.put( name, tempStr );
                                    }
                                }
                            }
                        }

                        llDataValue.setLineListValues( llElementValuesMap );
                        llDataValue.setPeriod( periodService.getPeriod( result.getInt( "periodid" )) );
                        llDataValue.setSource( organisationUnitService.getOrganisationUnit( result.getInt( "sourceid" )) );
                    }

                }
                //statement.close();
            } catch ( Exception e )
            {
                e.printStackTrace();
            }
        
        return llDataValue;

    }

    public Period getPeriodByRecordNumber( String tableName, int recordId )
    {
        String columnDefinition = "";

        columnDefinition += "select periodid from " + tableName + " where recordnumber = "+recordId;

        System.out.println( columnDefinition );
        Period tempPeriod = new Period();
        try
        {
            SqlRowSet result = jdbcTemplate.queryForRowSet( columnDefinition );
            if ( result != null )
            {
                result.beforeFirst();
                while ( result.next() )
                {
                    int tempPeriodId = result.getInt( 1 );
                    tempPeriod = periodService.getPeriod( tempPeriodId );
                    System.out.println("tempPeriodId = "+tempPeriodId);
                }
            }
        } 
        catch ( Exception e )
        {
            e.printStackTrace();
        }
        
        return tempPeriod;
    }
}
