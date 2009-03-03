package org.hisp.dhis.dd.action.extendedindicator;

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

import java.util.Date;

import org.hisp.dhis.datadictionary.ExtendedDataElement;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.expression.ExpressionService;
import org.hisp.dhis.i18n.I18nFormat;
import org.hisp.dhis.indicator.Indicator;
import org.hisp.dhis.indicator.IndicatorService;
import org.hisp.dhis.indicator.IndicatorType;

import com.opensymphony.xwork.ActionSupport;

import static org.hisp.dhis.system.util.TextUtils.nullIfEmpty;

/**
 * @author Lars Helge Overland
 * @version $Id$
 */
public class UpdateExtendedIndicatorAction
    extends ActionSupport
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private IndicatorService indicatorService;

    public void setIndicatorService( IndicatorService indicatorService )
    {
        this.indicatorService = indicatorService;
    }

    private ExpressionService expressionService;

    public void setExpressionService( ExpressionService expressionService )
    {
        this.expressionService = expressionService;
    }
    
    private I18nFormat format;

    public void setFormat( I18nFormat format )
    {
        this.format = format;
    }

    // -------------------------------------------------------------------------
    // Indicator
    // -------------------------------------------------------------------------

    private Integer id;

    public void setId( Integer id )
    {
        this.id = id;
    }

    private String name;

    public void setName( String name )
    {
        this.name = name;
    }

    private String shortName;

    public void setShortName( String shortName )
    {
        this.shortName = shortName;
    }

    private String alternativeName;

    public void setAlternativeName( String alternativeName )
    {
        this.alternativeName = alternativeName;
    }

    private String code;

    public void setCode( String code )
    {
        this.code = code;
    }

    private String description;

    public void setDescription( String description )
    {
        this.description = description;
    }

    private boolean annualized;

    public void setAnnualized( boolean annualized )
    {
        this.annualized = annualized;
    }

    private Integer indicatorTypeId;

    public void setIndicatorTypeId( Integer indicatorTypeId )
    {
        this.indicatorTypeId = indicatorTypeId;
    }

    private String numerator;

    public void setNumerator( String numerator )
    {
        this.numerator = numerator;
    }
    
    private String numeratorDescription;

    public void setNumeratorDescription( String numeratorDescription )
    {
        this.numeratorDescription = numeratorDescription;
    }

    private String numeratorAggregationOperator;

    public void setNumeratorAggregationOperator( String numeratorAggregationOperator )
    {
        this.numeratorAggregationOperator = numeratorAggregationOperator;
    }

    private String denominator;

    public void setDenominator( String denominator )
    {
        this.denominator = denominator;
    }

    private String denominatorDescription;

    public void setDenominatorDescription( String denominatorDescription )
    {
        this.denominatorDescription = denominatorDescription;
    }

    private String denominatorAggregationOperator;

    public void setDenominatorAggregationOperator( String denominatorAggregationOperator )
    {
        this.denominatorAggregationOperator = denominatorAggregationOperator;
    }

    // -------------------------------------------------------------------------
    // Identifying and Definitional attributes 
    // -------------------------------------------------------------------------
    
    private String mnemonic;

    public void setMnemonic( String mnemonic )
    {
        this.mnemonic = mnemonic;
    }    
    
    private String version;

    public void setVersion( String version )
    {
        this.version = version;
    }
    
    private String context;

    public void setContext( String context )
    {
        this.context = context;
    }    

    private String synonyms;

    public void setSynonyms( String synonyms )
    {
        this.synonyms = synonyms;
    }
    
    private String hononyms;

    public void setHononyms( String hononyms )
    {
        this.hononyms = hononyms;
    }
    
    private String keywords;

    public void setKeywords( String keywords )
    {
        this.keywords = keywords;
    }
    
    private String status;

    public void setStatus( String status )
    {
        this.status = status;
    }
    
    private String statusDate;

    public void setStatusDate( String statusDate )
    {
        this.statusDate = statusDate;
    }
    
    private String dataElementType;

    public void setDataElementType( String dataElementType )
    {
        this.dataElementType = dataElementType;
    }

    // -------------------------------------------------------------------------
    // Relational and Representational attributes
    // -------------------------------------------------------------------------

    private String dataType;

    public void setDataType( String dataType )
    {
        this.dataType = dataType;
    }

    private String representationalForm;

    public void setRepresentationalForm( String representationalForm )
    {
        this.representationalForm = representationalForm;
    }

    private String representationalLayout;

    public void setRepresentationalLayout( String representationalLayout )
    {
        this.representationalLayout = representationalLayout;
    }

    private Integer minimumSize;

    public void setMinimumSize( Integer minimumSize )
    {
        this.minimumSize = minimumSize;
    }
    
    private Integer maximumSize;

    public void setMaximumSize( Integer maximumSize )
    {
        this.maximumSize = maximumSize;
    }

    private String dataDomain;

    public void setDataDomain( String dataDomain )
    {
        this.dataDomain = dataDomain;
    }

    private String validationRules;

    public void setValidationRules( String validationRules )
    {
        this.validationRules = validationRules;
    }

    private String relatedDataReferences;

    public void setRelatedDataReferences( String relatedDataReferences )
    {
        this.relatedDataReferences = relatedDataReferences;
    }

    private String guideForUse;

    public void setGuideForUse( String guideForUse )
    {
        this.guideForUse = guideForUse;
    }

    private String collectionMethods;

    public void setCollectionMethods( String collectionMethods )
    {
        this.collectionMethods = collectionMethods;
    }

    // -------------------------------------------------------------------------
    // Administrative attributes 
    // -------------------------------------------------------------------------

    private String responsibleAuthority;

    public void setResponsibleAuthority( String responsibleAuthority )
    {
        this.responsibleAuthority = responsibleAuthority;
    }

    private String updateRules;

    public void setUpdateRules( String updateRules )
    {
        this.updateRules = updateRules;
    }

    private String accessAuthority;

    public void setAccessAuthority( String accessAuthority )
    {
        this.accessAuthority = accessAuthority;
    }

    private String updateFrequency;

    public void setUpdateFrequency( String updateFrequency )
    {
        this.updateFrequency = updateFrequency;
    }

    private String location;

    public void setLocation( String location )
    {
        this.location = location;
    }

    private String reportingMethods;

    public void setReportingMethods( String reportingMethods )
    {
        this.reportingMethods = reportingMethods;
    }

    private String versionStatus;

    public void setVersionStatus( String versionStatus )
    {
        this.versionStatus = versionStatus;
    }

    private String previousVersionReferences;

    public void setPreviousVersionReferences( String previousVersionReferences )
    {
        this.previousVersionReferences = previousVersionReferences;
    }

    private String sourceDocument;

    public void setSourceDocument( String sourceDocument )
    {
        this.sourceDocument = sourceDocument;
    }

    private String sourceOrganisation;

    public void setSourceOrganisation( String sourceOrganisation )
    {
        this.sourceOrganisation = sourceOrganisation;
    }

    private String comment;

    public void setComment( String comment )
    {
        this.comment = comment;
    }
    
    // -------------------------------------------------------------------------
    // Action implementation
    // -------------------------------------------------------------------------

    public String execute()
        throws Exception
    {
        // ---------------------------------------------------------------------
        // Prepare values
        // ---------------------------------------------------------------------

        if ( alternativeName != null && alternativeName.trim().length() == 0 )
        {
            alternativeName = null;
        }

        if ( code != null && code.trim().length() == 0 )
        {
            code = null;
        }
        
        if ( description != null && description.trim().length() == 0 )
        {
            description = null;
        }

        if ( numerator != null && numerator.trim().length() == 0 )
        {
            numerator = null;
        }

        if ( numeratorDescription != null && numeratorDescription.trim().length() == 0 )
        {
            numeratorDescription = null;
        }

        if ( numeratorAggregationOperator != null && numeratorAggregationOperator.trim().length() == 0 )
        {
            numeratorAggregationOperator = DataElement.AGGREGATION_OPERATOR_SUM;
        }

        if ( denominator != null && denominator.trim().length() == 0 )
        {
            denominator = null;
        }

        if ( denominatorDescription != null && denominatorDescription.trim().length() == 0 )
        {
            denominatorDescription = null;
        }

        if ( denominatorAggregationOperator != null && denominatorAggregationOperator.trim().length() == 0 )
        {
            denominatorAggregationOperator = DataElement.AGGREGATION_OPERATOR_SUM;
        }
        
        if ( context != null && context.trim().length() == 0 )
        {
            context = null;
        }
        
        if ( synonyms != null && synonyms.trim().length() == 0 )
        {
            synonyms = null;
        }
        
        if ( hononyms != null && hononyms.trim().length() == 0 )
        {
            hononyms = null;
        }
        
        if ( statusDate != null && statusDate.trim().length() == 0 )
        {
            statusDate = null;
        }
        
        if ( representationalLayout != null && representationalLayout.trim().length() == 0 )
        {
            representationalLayout = null;
        }
        
        if ( dataDomain != null && dataDomain.trim().length() == 0 )
        {
            dataDomain = null;
        }

        if ( validationRules != null && validationRules.trim().length() == 0 )
        {
            validationRules = null;
        }

        if ( relatedDataReferences != null && relatedDataReferences.trim().length() == 0 )
        {
            relatedDataReferences = null;
        }

        if ( guideForUse != null && guideForUse.trim().length() == 0 )
        {
            guideForUse = null;
        }

        if ( collectionMethods != null && collectionMethods.trim().length() == 0 )
        {
            collectionMethods = null;
        }

        if ( updateRules != null && updateRules.trim().length() == 0 )
        {
            updateRules = null;
        }

        if ( accessAuthority != null && accessAuthority.trim().length() == 0 )
        {
            accessAuthority = null;
        }

        if ( updateFrequency != null && updateFrequency.trim().length() == 0 )
        {
            updateFrequency = null;
        }

        if ( previousVersionReferences != null && previousVersionReferences.trim().length() == 0 )
        {
            previousVersionReferences = null;
        }

        if ( sourceDocument != null && sourceDocument.trim().length() == 0 )
        {
            sourceDocument = null;
        }

        if ( sourceOrganisation != null && sourceOrganisation.trim().length() == 0 )
        {
            sourceOrganisation = null;
        }

        if ( comment != null && comment.trim().length() == 0 )
        {
            comment = null;
        }
        
        Indicator indicator = indicatorService.getIndicator( id );

        IndicatorType indicatorType = indicatorService.getIndicatorType( indicatorTypeId );

        numerator = expressionService.replaceCDEsWithTheirExpression( numerator );
        
        denominator = expressionService.replaceCDEsWithTheirExpression( denominator );

        // -------------------------------------------------------------------------
        // Indicator
        // -------------------------------------------------------------------------

        indicator.setName( name );
        indicator.setAlternativeName( nullIfEmpty( alternativeName ) );
        indicator.setShortName( shortName );
        indicator.setCode( nullIfEmpty( code ) );
        indicator.setDescription( nullIfEmpty( description ) );
        indicator.setAnnualized( annualized );
        indicator.setIndicatorType( indicatorType );
        indicator.setNumerator( nullIfEmpty( numerator ) );
        indicator.setNumeratorDescription( nullIfEmpty( numeratorDescription ) );
        indicator.setNumeratorAggregationOperator( nullIfEmpty( numeratorAggregationOperator ) );
        indicator.setDenominator( nullIfEmpty( denominator ) );
        indicator.setDenominatorDescription( nullIfEmpty( denominatorDescription ) );
        indicator.setDenominatorAggregationOperator( nullIfEmpty( denominatorAggregationOperator ) );
        
        ExtendedDataElement extendedDataElement = new ExtendedDataElement();
        
        // -------------------------------------------------------------------------
        // Identifying and Definitional attributes 
        // -------------------------------------------------------------------------
        
        extendedDataElement.setMnemonic( nullIfEmpty( mnemonic ) );
        extendedDataElement.setVersion( nullIfEmpty( version ) );
        extendedDataElement.setContext( nullIfEmpty( context ) );
        extendedDataElement.setSynonyms( nullIfEmpty( synonyms ) );
        extendedDataElement.setHononyms( nullIfEmpty( hononyms ) );
        extendedDataElement.setKeywords( nullIfEmpty( keywords ) );
        extendedDataElement.setStatus( nullIfEmpty( status ) );
        extendedDataElement.setStatusDate( format.parseDate( statusDate ) );
        extendedDataElement.setDataElementType( nullIfEmpty( dataElementType ) );
        
        // -------------------------------------------------------------------------
        // Relational and Representational attributes
        // -------------------------------------------------------------------------

        extendedDataElement.setDataType( nullIfEmpty( dataType ) );
        extendedDataElement.setRepresentationalForm( nullIfEmpty( representationalForm ) );
        extendedDataElement.setRepresentationalLayout( nullIfEmpty( representationalLayout ) );
        extendedDataElement.setMinimumSize( minimumSize );
        extendedDataElement.setMaximumSize( maximumSize );
        extendedDataElement.setDataDomain( nullIfEmpty( dataDomain ) );
        extendedDataElement.setValidationRules( nullIfEmpty( validationRules ) );
        extendedDataElement.setRelatedDataReferences( nullIfEmpty( relatedDataReferences ) );
        extendedDataElement.setGuideForUse( nullIfEmpty( guideForUse ) );
        extendedDataElement.setCollectionMethods( nullIfEmpty( collectionMethods ) );

        // -------------------------------------------------------------------------
        // Administrative attributes 
        // -------------------------------------------------------------------------

        extendedDataElement.setResponsibleAuthority( nullIfEmpty( responsibleAuthority ) );
        extendedDataElement.setUpdateRules( nullIfEmpty( updateRules ) );
        extendedDataElement.setAccessAuthority( nullIfEmpty( accessAuthority ) );
        extendedDataElement.setUpdateFrequency( nullIfEmpty( updateFrequency ) );
        extendedDataElement.setLocation( nullIfEmpty( location ) );
        extendedDataElement.setReportingMethods( nullIfEmpty( reportingMethods ) );
        extendedDataElement.setVersionStatus( nullIfEmpty( versionStatus ) );
        extendedDataElement.setPreviousVersionReferences( nullIfEmpty( previousVersionReferences ) );
        extendedDataElement.setSourceDocument( nullIfEmpty( sourceDocument ) );
        extendedDataElement.setSourceOrganisation( nullIfEmpty( sourceOrganisation ) );
        extendedDataElement.setComment( nullIfEmpty( comment ) );
        extendedDataElement.setSaved( new Date() );
        extendedDataElement.setLastUpdated( new Date() );
        
        indicator.setExtended( extendedDataElement );
        
        indicatorService.updateIndicator( indicator );
        
        return SUCCESS;
    }
}
