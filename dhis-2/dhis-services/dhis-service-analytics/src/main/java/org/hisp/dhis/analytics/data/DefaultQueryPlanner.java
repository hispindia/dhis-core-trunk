package org.hisp.dhis.analytics.data;

/*
 * Copyright (c) 2004-2016, University of Oslo
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hisp.dhis.analytics.AggregationType;
import org.hisp.dhis.analytics.DataQueryGroups;
import org.hisp.dhis.analytics.DataQueryParams;
import org.hisp.dhis.analytics.DataType;
import org.hisp.dhis.analytics.Partitions;
import org.hisp.dhis.analytics.QueryPlanner;
import org.hisp.dhis.analytics.QueryPlannerParams;
import org.hisp.dhis.analytics.partition.PartitionManager;
import org.hisp.dhis.analytics.table.PartitionUtils;
import org.hisp.dhis.common.BaseDimensionalObject;
import org.hisp.dhis.common.DimensionType;
import org.hisp.dhis.common.DimensionalItemObject;
import org.hisp.dhis.common.DimensionalObject;
import org.hisp.dhis.common.IllegalQueryException;
import org.hisp.dhis.common.ListMap;
import org.hisp.dhis.common.MaintenanceModeException;
import org.hisp.dhis.commons.collection.PaginatedList;
import org.hisp.dhis.commons.filter.FilterUtils;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementGroup;
import org.hisp.dhis.period.PeriodType;
import org.hisp.dhis.program.ProgramDataElement;
import org.hisp.dhis.setting.SettingKey;
import org.hisp.dhis.setting.SystemSettingManager;
import org.hisp.dhis.system.filter.AggregatableDataElementFilter;
import org.hisp.dhis.system.util.MathUtils;
import org.hisp.dhis.util.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import static org.hisp.dhis.analytics.AggregationType.SUM;
import static org.hisp.dhis.analytics.DataQueryParams.LEVEL_PREFIX;
import static org.hisp.dhis.common.DimensionalObject.*;
import static org.hisp.dhis.common.IdentifiableObjectUtils.getUids;
import static org.hisp.dhis.common.DimensionalObjectUtils.asTypedList;

/**
 * @author Lars Helge Overland
 */
public class DefaultQueryPlanner
    implements QueryPlanner
{
    private static final Log log = LogFactory.getLog( DefaultQueryPlanner.class );

    @Autowired
    private PartitionManager partitionManager;

    @Autowired
    private SystemSettingManager systemSettingManager;

    // -------------------------------------------------------------------------
    // DefaultQueryPlanner implementation
    // -------------------------------------------------------------------------

    //TODO shortcut group by methods when only 1 option?

    @Override
    public void validate( DataQueryParams params )
        throws IllegalQueryException
    {
        String violation = null;

        if ( params == null )
        {
            throw new IllegalQueryException( "Params cannot be null" );
        }

        final List<DimensionalItemObject> dataElements = Lists.newArrayList( params.getDataElements() );
        params.getProgramDataElements().stream().forEach( pde -> dataElements.add( ((ProgramDataElement) pde).getDataElement() ) );        
        final List<DataElement> nonAggDataElements = FilterUtils.inverseFilter( asTypedList( dataElements ), AggregatableDataElementFilter.INSTANCE );

        if ( params.getDimensions().isEmpty() )
        {
            violation = "At least one dimension must be specified";
        }

        if ( !params.getDimensionsAsFilters().isEmpty() )
        {
            violation = "Dimensions cannot be specified as dimension and filter simultaneously: " + params.getDimensionsAsFilters();
        }

        if ( !params.hasPeriods() && !params.isSkipPartitioning() )
        {
            violation = "At least one period must be specified as dimension or filter";
        }

        if ( !params.getFilterIndicators().isEmpty() )
        {
            violation = "Indicators cannot be specified as filter";
        }

        if ( !params.getFilterReportingRates().isEmpty() )
        {
            violation = "Reporting rates cannot be specified as filter";
        }

        if ( params.getFilters().contains( new BaseDimensionalObject( CATEGORYOPTIONCOMBO_DIM_ID ) ) )
        {
            violation = "Category option combos cannot be specified as filter";
        }

        if ( !params.getDuplicateDimensions().isEmpty() )
        {
            violation = "Dimensions cannot be specified more than once: " + params.getDuplicateDimensions();
        }

        if ( !params.getAllReportingRates().isEmpty() && !params.getDataElementGroupSets().isEmpty() )
        {
            violation = "Reporting rates and data element group sets cannot be specified simultaneously";
        }

        if ( params.hasDimensionOrFilter( CATEGORYOPTIONCOMBO_DIM_ID ) && params.getAllDataElements().isEmpty() )
        {
            violation = "Category option combos cannot be specified when data elements are not specified";
        }

        if ( !nonAggDataElements.isEmpty() )
        {
            violation = "Data elements must be of a type that allows aggregation: " + getUids( nonAggDataElements );
        }

        if ( violation != null )
        {
            log.warn( "Analytics validation failed: " + violation );

            throw new IllegalQueryException( violation );
        }
    }

    @Override
    public void validateTableLayout( DataQueryParams params, List<String> columns, List<String> rows )
    {
        String violation = null;

        if ( (columns == null || columns.isEmpty()) && (rows == null || rows.isEmpty()) )
        {
            violation = "Cannot generate table layout when columns and rows are empty";
        }

        if ( columns != null )
        {
            for ( String column : columns )
            {
                if ( !params.hasDimension( column ) )
                {
                    violation = "Column must be present as dimension in query: " + column;
                }
            }
        }

        if ( rows != null )
        {
            for ( String row : rows )
            {
                if ( !params.hasDimension( row ) )
                {
                    violation = "Row must be present as dimension in query: " + row;
                }
            }
        }

        if ( violation != null )
        {
            log.warn( "Validation failed: " + violation );

            throw new IllegalQueryException( violation );
        }
    }

    @Override
    public void validateMaintenanceMode()
        throws MaintenanceModeException
    {
        boolean maintenance = (Boolean) systemSettingManager.getSystemSetting( SettingKey.ANALYTICS_MAINTENANCE_MODE );

        if ( maintenance )
        {
            throw new MaintenanceModeException( "Analytics engine is in maintenance mode, try again later" );
        }
    }

    @Override
    public DataQueryGroups planQuery( DataQueryParams params, final int optimalQueries, final String tableName )
    {
        validate( params );
        
        // ---------------------------------------------------------------------
        // Group queries which can be executed together
        // ---------------------------------------------------------------------

        params = params.instance();

        final QueryPlannerParams plannerParams = QueryPlannerParams.instance().setTableName( tableName );

        final List<DataQueryParams> queries = new ArrayList<>( groupByPartition( params, plannerParams ) );
        
        List<Consumer<DataQueryParams>> groupers = new ImmutableList.Builder<Consumer<DataQueryParams>>().
            add( q -> queries.addAll( groupByOrgUnitLevel( q ) ) ).
            add( q -> queries.addAll( groupByPeriodType( q ) ) ).
            add( q -> queries.addAll( groupByDataType( q ) ) ).
            add( q -> queries.addAll( groupByAggregationType( q ) ) ).
            add( q -> queries.addAll( groupByDaysInPeriod( q ) ) ).
            add( q -> queries.addAll( groupByDataPeriodType( q ) ) ).build();
        
        for ( Consumer<DataQueryParams> grouper : groupers )
        {
            final List<DataQueryParams> currentQueries = Lists.newArrayList( queries );
            queries.clear();
            
            currentQueries.stream().forEach( grouper );
        }

        // ---------------------------------------------------------------------
        // Split queries until optimal number
        // ---------------------------------------------------------------------

        DataQueryGroups queryGroups = new DataQueryGroups( queries );

        if ( queryGroups.isOptimal( optimalQueries ) )
        {
            return queryGroups;
        }

        List<String> splitDimensions = Lists.newArrayList( DATA_X_DIM_ID, ORGUNIT_DIM_ID );
        
        for ( String dim : splitDimensions )
        {
            queryGroups = splitByDimension( queryGroups, dim, optimalQueries );

            if ( queryGroups.isOptimal( optimalQueries ) )
            {
                break;
            }
        }
        
        return queryGroups;
    }

    // -------------------------------------------------------------------------
    // Supportive methods
    // -------------------------------------------------------------------------

    /**
     * Splits the given list of queries in sub queries on the given dimension.
     */
    private DataQueryGroups splitByDimension( DataQueryGroups queryGroups, String dimension, int optimalQueries )
    {
        int optimalForSubQuery = MathUtils.divideToFloor( optimalQueries, queryGroups.getLargestGroupSize() );

        List<DataQueryParams> subQueries = new ArrayList<>();

        for ( DataQueryParams query : queryGroups.getAllQueries() )
        {
            DimensionalObject dim = query.getDimension( dimension );

            List<DimensionalItemObject> values = null;

            if ( dim == null || (values = dim.getItems()) == null || values.isEmpty() )
            {
                subQueries.add( query.instance() );
                continue;
            }

            List<List<DimensionalItemObject>> valuePages = new PaginatedList<>( values ).setNumberOfPages( optimalForSubQuery ).getPages();

            for ( List<DimensionalItemObject> valuePage : valuePages )
            {
                DataQueryParams subQuery = query.instance();
                subQuery.setDimensionOptions( dim.getDimension(), dim.getDimensionType(), dim.getDimensionName(), valuePage );
                subQueries.add( subQuery );
            }
        }

        if ( subQueries.size() > queryGroups.getAllQueries().size() )
        {
            log.debug( "Split on dimension " + dimension + ": " + (subQueries.size() / queryGroups.getAllQueries().size()) );
        }

        return new DataQueryGroups( subQueries );
    }

    // -------------------------------------------------------------------------
    // Supportive - group by methods
    // -------------------------------------------------------------------------

    @Override
    public List<DataQueryParams> groupByPartition( DataQueryParams params, QueryPlannerParams plannerParams )
    {
        Set<String> validPartitions = partitionManager.getAnalyticsPartitions();
        
        String tableName = plannerParams.getTableName();
        String tableSuffix = plannerParams.getTableSuffix();

        List<DataQueryParams> queries = new ArrayList<>();

        if ( params.isSkipPartitioning() )
        {
            params.setPartitions( new Partitions().add( tableName ) );
            queries.add( params );
        }
        else if ( !params.getPeriods().isEmpty() )
        {
            ListMap<Partitions, DimensionalItemObject> partitionPeriodMap = 
                PartitionUtils.getPartitionPeriodMap( params.getPeriods(), tableName, tableSuffix, validPartitions );

            for ( Partitions partitions : partitionPeriodMap.keySet() )
            {
                if ( partitions.hasAny() )
                {
                    DataQueryParams query = params.instance();
                    query.setPeriods( partitionPeriodMap.get( partitions ) );
                    query.setPartitions( partitions );
                    queries.add( query );
                }
            }
        }
        else if ( !params.getFilterPeriods().isEmpty() )
        {
            Partitions partitions = PartitionUtils.getPartitions( params.getFilterPeriods(), tableName, tableSuffix, validPartitions );

            if ( partitions.hasAny() )
            {
                DataQueryParams query = params.instance();
                query.setPartitions( partitions );
                queries.add( query );
            }
        }
        else
        {
            throw new IllegalQueryException( "Query does not contain any period dimension items" );
        }

        if ( queries.size() > 1 )
        {
            log.debug( "Split on partition: " + queries.size() );
        }

        return queries;
    }

    /**
     * If periods appear as dimensions in the given query; groups the query into
     * sub queries based on the period type of the periods. Sets the period type
     * name on each query. If periods appear as filters; replaces the period filter
     * with one filter for each period type. Sets the dimension names and filter
     * names respectively.
     */
    @Override
    public List<DataQueryParams> groupByPeriodType( DataQueryParams params )
    {
        List<DataQueryParams> queries = new ArrayList<>();

        if ( params.isSkipPartitioning() )
        {
            queries.add( params );
        }
        else if ( !params.getPeriods().isEmpty() )
        {
            ListMap<String, DimensionalItemObject> periodTypePeriodMap = PartitionUtils.getPeriodTypePeriodMap( params.getPeriods() );

            for ( String periodType : periodTypePeriodMap.keySet() )
            {
                DataQueryParams query = params.instance();
                query.setDimensionOptions( PERIOD_DIM_ID, DimensionType.PERIOD, periodType.toLowerCase(), periodTypePeriodMap.get( periodType ) );
                query.setPeriodType( periodType );
                queries.add( query );
            }
        }
        else if ( !params.getFilterPeriods().isEmpty() )
        {
            DimensionalObject filter = params.getFilter( PERIOD_DIM_ID );

            ListMap<String, DimensionalItemObject> periodTypePeriodMap = PartitionUtils.getPeriodTypePeriodMap( filter.getItems() );

            params.removeFilter( PERIOD_DIM_ID ).setPeriodType( periodTypePeriodMap.keySet().iterator().next() ); // Using first period type

            for ( String periodType : periodTypePeriodMap.keySet() )
            {
                params.getFilters().add( new BaseDimensionalObject( filter.getDimension(),
                    filter.getDimensionType(), periodType.toLowerCase(), filter.getDisplayName(), periodTypePeriodMap.get( periodType ) ) );
            }

            queries.add( params );
        }
        else
        {
            queries.add( params.instance() );
            return queries;
        }

        if ( queries.size() > 1 )
        {
            log.debug( "Split on period type: " + queries.size() );
        }

        return queries;
    }

    @Override
    public List<DataQueryParams> groupByOrgUnitLevel( DataQueryParams params )
    {
        List<DataQueryParams> queries = new ArrayList<>();

        if ( !params.getOrganisationUnits().isEmpty() )
        {
            ListMap<Integer, DimensionalItemObject> levelOrgUnitMap = 
                QueryPlannerUtils.getLevelOrgUnitMap( params.getOrganisationUnits() );

            for ( Integer level : levelOrgUnitMap.keySet() )
            {
                DataQueryParams query = params.instance();
                query.setDimensionOptions( ORGUNIT_DIM_ID, DimensionType.ORGANISATION_UNIT, LEVEL_PREFIX + level, levelOrgUnitMap.get( level ) );
                queries.add( query );
            }
        }
        else if ( !params.getFilterOrganisationUnits().isEmpty() )
        {
            DimensionalObject filter = params.getFilter( ORGUNIT_DIM_ID );

            ListMap<Integer, DimensionalItemObject> levelOrgUnitMap = 
                QueryPlannerUtils.getLevelOrgUnitMap( params.getFilterOrganisationUnits() );

            params.removeFilter( ORGUNIT_DIM_ID );

            for ( Integer level : levelOrgUnitMap.keySet() )
            {
                params.getFilters().add( new BaseDimensionalObject( filter.getDimension(),
                    filter.getDimensionType(), LEVEL_PREFIX + level, filter.getDisplayName(), levelOrgUnitMap.get( level ) ) );
            }

            queries.add( params );
        }
        else
        {
            queries.add( params.instance() );
            return queries;
        }

        if ( queries.size() > 1 )
        {
            log.debug( "Split on org unit level: " + queries.size() );
        }

        return queries;
    }

    /**
     * Groups queries by their data type.
     */
    private List<DataQueryParams> groupByDataType( DataQueryParams params )
    {
        List<DataQueryParams> queries = new ArrayList<>();

        if ( !params.getDataElements().isEmpty() )
        {
            ListMap<DataType, DimensionalItemObject> dataTypeDataElementMap = 
                QueryPlannerUtils.getDataTypeDataElementMap( params.getDataElements() );

            for ( DataType dataType : dataTypeDataElementMap.keySet() )
            {
                DataQueryParams query = params.instance();
                query.setDataElements( dataTypeDataElementMap.get( dataType ) );
                query.setDataType( dataType );
                queries.add( query );
            }
        }
        else
        {
            DataQueryParams query = params.instance();
            query.setDataType( DataType.NUMERIC );
            queries.add( query );
        }

        if ( queries.size() > 1 )
        {
            log.debug( "Split on data type: " + queries.size() );
        }

        return queries;
    }

    /**
     * Groups the given query in sub queries based on the aggregation type of its
     * data elements. The aggregation type can be sum, average aggregation or
     * average disaggregation. Sum means that the data elements have sum aggregation
     * operator. Average aggregation means that the data elements have the average
     * aggregation operator and that the period type of the data elements have
     * higher or equal frequency than the aggregation period type. Average disaggregation
     * means that the data elements have the average aggregation operator and
     * that the period type of the data elements have lower frequency than the
     * aggregation period type. Average bool means that the data elements have the
     * average aggregation operator and the bool value type.
     * <p>
     * If no data elements are present, the aggregation type will be determined
     * based on the first data element in the first data element group in the
     * first data element group set in the query.
     * <p>
     * If the aggregation type is already set/overridden in the request, the
     * query will be returned unchanged. If there are no data elements or data
     * element group sets specified the aggregation type will fall back to sum.
     */
    private List<DataQueryParams> groupByAggregationType( DataQueryParams params )
    {
        List<DataQueryParams> queries = new ArrayList<>();

        if ( !params.getDataElements().isEmpty() )
        {
            ListMap<AggregationType, DimensionalItemObject> aggregationTypeDataElementMap = 
                QueryPlannerUtils.getAggregationTypeDataElementMap( params );

            for ( AggregationType aggregationType : aggregationTypeDataElementMap.keySet() )
            {
                DataQueryParams query = params.instance();
                query.setDataElements( aggregationTypeDataElementMap.get( aggregationType ) );
                query.setAggregationType( aggregationType );
                queries.add( query );
            }
        }
        else if ( !params.getDataElementGroupSets().isEmpty() )
        {
            DimensionalObject degs = params.getDataElementGroupSets().get( 0 );
            DataElementGroup deg = (DataElementGroup) (degs.hasItems() ? degs.getItems().get( 0 ) : null);

            DataQueryParams query = params.instance();

            if ( deg != null && !deg.getMembers().isEmpty() )
            {
                PeriodType periodType = PeriodType.getPeriodTypeByName( params.getPeriodType() );
                AggregationType aggregationType = ObjectUtils.firstNonNull( params.getAggregationType(), deg.getAggregationType() );
                query.setAggregationType( QueryPlannerUtils.getAggregationType( 
                    deg.getValueType(), aggregationType, periodType, deg.getPeriodType() ) );
            }
            else
            {
                query.setAggregationType( ObjectUtils.firstNonNull( params.getAggregationType(), SUM ) );
            }

            queries.add( query );
        }
        else
        {
            DataQueryParams query = params.instance();
            query.setAggregationType( ObjectUtils.firstNonNull( params.getAggregationType(), SUM ) );
            queries.add( query );
        }

        if ( queries.size() > 1 )
        {
            log.debug( "Split on aggregation type: " + queries.size() );
        }

        return queries;
    }

    /**
     * Groups the given query into sub queries based on the number of days in the
     * aggregation period. This only applies if the aggregation type is
     * AVERAGE_SUM_INT and the query has at least one period as dimension option.
     * This is necessary since the number of days in the aggregation period is
     * part of the expression for aggregating the value.
     */
    private List<DataQueryParams> groupByDaysInPeriod( DataQueryParams params )
    {
        List<DataQueryParams> queries = new ArrayList<>();

        if ( params.getPeriods().isEmpty() || !params.isAggregationType( AggregationType.AVERAGE_SUM_INT ) )
        {
            queries.add( params.instance() );
            return queries;
        }

        ListMap<Integer, DimensionalItemObject> daysPeriodMap = 
            QueryPlannerUtils.getDaysPeriodMap( params.getPeriods() );

        DimensionalObject periodDim = params.getDimension( PERIOD_DIM_ID );

        for ( Integer days : daysPeriodMap.keySet() )
        {
            DataQueryParams query = params.instance();
            query.setDimensionOptions( periodDim.getDimension(), periodDim.getDimensionType(), periodDim.getDimensionName(), daysPeriodMap.get( days ) );
            queries.add( query );
        }

        if ( queries.size() > 1 )
        {
            log.debug( "Split on days in period: " + queries.size() );
        }

        return queries;
    }

    /**
     * Groups the given query in sub queries based on the period type of its
     * data elements. Sets the data period type on each query. This only applies
     * if the aggregation type of the query involves disaggregation.
     */
    private List<DataQueryParams> groupByDataPeriodType( DataQueryParams params )
    {
        List<DataQueryParams> queries = new ArrayList<>();

        if ( params.getDataElements().isEmpty() || !params.isDisaggregation() )
        {
            queries.add( params.instance() );
            return queries;
        }

        ListMap<PeriodType, DimensionalItemObject> periodTypeDataElementMap = 
            QueryPlannerUtils.getPeriodTypeDataElementMap( params.getDataElements() );

        for ( PeriodType periodType : periodTypeDataElementMap.keySet() )
        {
            DataQueryParams query = params.instance();
            query.setDataElements( periodTypeDataElementMap.get( periodType ) );
            query.setDataPeriodType( periodType );
            queries.add( query );
        }

        if ( queries.size() > 1 )
        {
            log.debug( "Split on data period type: " + queries.size() );
        }

        return queries;
    }
}
