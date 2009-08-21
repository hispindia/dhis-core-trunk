// reference local blank image
Ext.BLANK_IMAGE_URL = '../../mfbase/ext/resources/images/default/s.gif';

Ext.onReady(function()
{
    Ext.state.Manager.setProvider(new Ext.state.CookieProvider());
    
    Ext.override(Ext.layout.FormLayout, {
        renderItem : function(c, position, target) {
            if (c && !c.rendered && c.isFormField && c.inputType != 'hidden') {
                var args = [
                    c.id, c.fieldLabel,
                    c.labelStyle||this.labelStyle||'',
                    this.elementStyle||'',
                    typeof c.labelSeparator == 'undefined' ? this.labelSeparator : c.labelSeparator,
                    (c.itemCls||this.container.itemCls||'') + (c.hideLabel ? ' x-hide-label' : ''),
                    c.clearCls || 'x-form-clear-left' 
                ];
                
                if (typeof position == 'number') {
                    position = target.dom.childNodes[position] || null;
                }
                
                if (position) {
                    c.formItem = this.fieldTpl.insertBefore(position, args, true);
                }
                else {
                    c.formItem = this.fieldTpl.append(target, args, true);
                }
                c.actionMode = 'formItem';
                c.render('x-form-el-'+c.id);
                c.container = c.formItem;
                c.actionMode = 'container';
            }
            else {
                Ext.layout.FormLayout.superclass.renderItem.apply(this, arguments);
            }
        }
    });

    Ext.override(Ext.form.TriggerField, {
        actionMode: 'wrap',
        onShow: Ext.form.TriggerField.superclass.onShow,
        onHide: Ext.form.TriggerField.superclass.onHide
    });
    
    Ext.override(Ext.form.Checkbox, {
        onRender: function(ct, position) {
            Ext.form.Checkbox.superclass.onRender.call(this, ct, position);
            if(this.inputValue !== undefined) {
                this.el.dom.value = this.inputValue;
            }
            //this.el.addClass('x-hidden');
            this.innerWrap = this.el.wrap({
                //tabIndex: this.tabIndex,
                cls: this.baseCls+'-wrap-inner'
            });
            
            this.wrap = this.innerWrap.wrap({cls: this.baseCls+'-wrap'});
            
            this.imageEl = this.innerWrap.createChild({
                tag: 'img',
                src: Ext.BLANK_IMAGE_URL,
                cls: this.baseCls
            });
            
            if(this.boxLabel){
                this.labelEl = this.innerWrap.createChild({
                    tag: 'label',
                    htmlFor: this.el.id,
                    cls: 'x-form-cb-label',
                    html: this.boxLabel
                });
            }
            //this.imageEl = this.innerWrap.createChild({
                //tag: 'img',
                //src: Ext.BLANK_IMAGE_URL,
                //cls: this.baseCls
            //}, this.el);
            if(this.checked) {
                this.setValue(true);
            }
            else {
                this.checked = this.el.dom.checked;
            }
            this.originalValue = this.checked;
        },
        afterRender: function() {
            Ext.form.Checkbox.superclass.afterRender.call(this);
            //this.wrap[this.checked ? 'addClass' : 'removeClass'](this.checkedCls);
            this.imageEl[this.checked ? 'addClass' : 'removeClass'](this.checkedCls);
        },
        initCheckEvents: function() {
            //this.innerWrap.removeAllListeners();
            this.innerWrap.addClassOnOver(this.overCls);
            this.innerWrap.addClassOnClick(this.mouseDownCls);
            this.innerWrap.on('click', this.onClick, this);
            //this.innerWrap.on('keyup', this.onKeyUp, this);
        },
        onFocus: function(e) {
            Ext.form.Checkbox.superclass.onFocus.call(this, e);
            //this.el.addClass(this.focusCls);
            this.innerWrap.addClass(this.focusCls);
        },
        onBlur: function(e) {
            Ext.form.Checkbox.superclass.onBlur.call(this, e);
            //this.el.removeClass(this.focusCls);
            this.innerWrap.removeClass(this.focusCls);
        },
        onClick: function(e) {
            if (e.getTarget().htmlFor != this.el.dom.id) {
                if (e.getTarget() !== this.el.dom) {
                    this.el.focus();
                }
                if (!this.disabled && !this.readOnly) {
                    this.toggleValue();
                }
            }
            //e.stopEvent();
        },
        onEnable: Ext.form.Checkbox.superclass.onEnable,
        onDisable: Ext.form.Checkbox.superclass.onDisable,
        onKeyUp: undefined,
        setValue: function(v) {
            var checked = this.checked;
            this.checked = (v === true || v === 'true' || v == '1' || String(v).toLowerCase() == 'on');
            if (this.rendered) {
                this.el.dom.checked = this.checked;
                this.el.dom.defaultChecked = this.checked;
                //this.wrap[this.checked ? 'addClass' : 'removeClass'](this.checkedCls);
                this.imageEl[this.checked ? 'addClass' : 'removeClass'](this.checkedCls);
            }
            if (checked != this.checked) {
                this.fireEvent("check", this, this.checked);
                if(this.handler){
                    this.handler.call(this.scope || this, this, this.checked);
                }
            }
        },
        getResizeEl: function() {
            //if(!this.resizeEl){
                //this.resizeEl = Ext.isSafari ? this.wrap : (this.wrap.up('.x-form-element', 5) || this.wrap);
            //}
            //return this.resizeEl;
            return this.wrap;
        }
    });
    
    Ext.override(Ext.form.Radio, {
        checkedCls: 'x-form-radio-checked'
    });

    myMap: null;
    map = new OpenLayers.Map($('olmap'));
    this.myMap = map;

    MASK = new Ext.LoadMask(Ext.getBody(), {msg:"Please wait..."});
    
    MAPDATA = null;
    URL = null;
    ACTIVEPANEL = 'choropleth';
    MAPVIEW = false;
    MAPVIEWACTIVE = false;    
    URLACTIVE = false;
    PARAMETER = null;
    BOUNDS = 0;
    STATIC1LOADED = false;
    MAP_SOURCE_TYPE_DATABASE = 'database';
    MAP_SOURCE_TYPE_SHAPEFILE = 'shapefile';
    MAPSOURCE = null;
    
    Ext.Ajax.request({
        url: path + 'getMapSourceTypeUserSetting' + type,
        method: 'GET',
        
        success: function( responseObject ) {
            MAPSOURCE = Ext.util.JSON.decode( responseObject.responseText ).mapSource;
        },
        failure: function() {
            alert( 'Status', 'Error while saving data' );
        }
    });
    
    function getUrlParam(strParamName) {
        var output = "";
        var strHref = window.location.href;
        if ( strHref.indexOf("?") > -1 ) {
            var strQueryString = strHref.substr(strHref.indexOf("?")).toLowerCase();
            var aQueryString = strQueryString.split("&");
            for ( var iParam = 0; iParam < aQueryString.length; iParam++ ) {
                if (aQueryString[iParam].indexOf(strParamName.toLowerCase() + "=") > -1 ) {
                    var aParam = aQueryString[iParam].split("=");
                    output = aParam[1];
                    break;
                }
            }
        }
        return unescape(output);
    }
    
    if (getUrlParam('view') != '') {
        PARAMETER = getUrlParam('view');
        URLACTIVE = true;
    }
    
    function validateInput(name) {
        if (name.length > 25) {
            return false;
        }
        else {
            return true;
        }
    }
    
    function getMultiSelectHeight() {
        var h = screen.height;
        
        if (h <= 800) {
            return 120;
        }
        else if (h <= 1050) {
            return 310;
        }
        else if (h <= 1200) {
            return 530;
        }
        else {
            return 900;
        }
    }

    var vmap0 = new OpenLayers.Layer.WMS(
        "OpenLayers WMS",
        "http://labs.metacarta.com/wms/vmap0", 
        {layers: 'basic'}
    );
                                           
    var local_wfs = new OpenLayers.Layer.WMS(
        "Africa",
        "../../../geoserver/wfs?", 
        {layers: 'world:africa'}
    );
                                                 
/*    var jpl_wms = new OpenLayers.Layer.WMS("Satellite",
                                           "http://demo.opengeo.org/geoserver/wms", 
                                           {layers: 'bluemarble', format: 'image/png'});
*/                                   
    var choroplethLayer = new OpenLayers.Layer.Vector(CHOROPLETH_LAYERNAME, {
        'visibility': true,
        'styleMap': new OpenLayers.StyleMap({
            'default': new OpenLayers.Style(
                OpenLayers.Util.applyDefaults(
                    {'fillOpacity': 1, 'strokeColor': '#222222', 'strokeWidth': 1},
                    OpenLayers.Feature.Vector.style['default']
                )
            ),
            'select': new OpenLayers.Style(
                {'strokeColor': '#000000', 'strokeWidth': 2, 'cursor': 'pointer'}
            )
        })
    });
    
    var static1Layer = new OpenLayers.Layer.Vector(STATIC1_LAYERNAME, {
        'visibility': false,
        'styleMap': new OpenLayers.StyleMap({
            'default': new OpenLayers.Style(
                OpenLayers.Util.applyDefaults(
                    {'fillOpacity': 1, 'strokeWidth': 2, 'strokeColor': '#000000', 'strokeOpacity': 1 },
                    OpenLayers.Feature.Vector.style['default']
                )
            )
        })
    });
    
    map.addLayers([ vmap0, local_wfs, choroplethLayer, static1Layer ]);

    var selectFeatureChoropleth = new OpenLayers.Control.newSelectFeature(
        choroplethLayer,
        {
            onClickSelect: onClickSelectChoropleth,
            onClickUnselect: onClickUnselectChoropleth,
            onHoverSelect: onHoverSelectChoropleth,
            onHoverUnselect: onHoverUnselectChoropleth
        }
    );
    
    map.addControl(selectFeatureChoropleth);
    selectFeatureChoropleth.activate();

    map.setCenter(new OpenLayers.LonLat(init_longitude, init_latitude), init_zoom);
    
    // REGISTER SHAPEFILE PANEL
    
    var organisationUnitLevelStore = new Ext.data.JsonStore({
        url: path + 'getOrganisationUnitLevels' + type,
        baseParams: { format: 'json' },
        root: 'organisationUnitLevels',
        fields: ['id', 'level', 'name'],
        autoLoad: true
    });

    var organisationUnitStore = new Ext.data.JsonStore({
        url: path + 'getOrganisationUnitsAtLevel' + type,
        baseParams: { level: 1, format: 'json' },
        root: 'organisationUnits',
        fields: ['id', 'name'],
        sortInfo: { field: 'name', direction: 'ASC' },
        autoLoad: false
    });
    
    var existingMapsStore = new Ext.data.JsonStore({
            url: path + 'getAllMaps' + type,
            baseParams: { format: 'jsonmin' },
            root: 'maps',
            fields: ['id', 'name', 'mapLayerPath', 'organisationUnitLevel'],
            autoLoad: true
    });

    var organisationUnitComboBox = new Ext.form.ComboBox({
        id: 'organisationunit_cb',
        fieldLabel: 'Organisation unit',
        typeAhead: true,
        editable: false,
        valueField: 'id',
        displayField: 'name',
        emptyText: 'Required',
        mode: 'remote',
        forceSelection: true,
        triggerAction: 'all',
        selectOnFocus: true,
        width: combo_width,
        minListWidth: combo_width + 26,
        store: organisationUnitStore
    });
    
    var organisationUnitLevelComboBox = new Ext.form.ComboBox({
        id: 'organisationunitlevel_cb',
        fieldLabel: 'Level',
        typeAhead: true,
        editable: false,
        valueField: 'id',
        displayField: 'name',
        emptyText: 'Required',
        mode: 'remote',
        forceSelection: true,
        triggerAction: 'all',
        selectOnFocus: true,
        width: combo_width,
        minListWidth: combo_width + 26,
        store: organisationUnitLevelStore,
        listeners: {
            'select': {
                fn: function() {
                    var level1 = Ext.getCmp('newmap_cb').getValue();
                    var level2 = Ext.getCmp('organisationunitlevel_cb').getValue();
                    var orgunit = Ext.getCmp('organisationunit_cb').getValue();

                    if (level1 >= level2) { // CURRENTLY NOT WORKING BECAUSE OF valuefield: 'id'
                        organisationUnitLevelComboBox.reset();
                        Ext.messageRed.msg('New map', 'The organisation unit selected above must be divided into a lower level than itself.');
                        return;
                    }
                },
                scope: this
            }
        }
    });

    var newNameTextField = new Ext.form.TextField({
        id: 'newname_tf',
        emptyText: 'Required',
        width: combo_width
    });
    
    var editNameTextField = new Ext.form.TextField({
        id: 'editname_tf',
        emptyText: '',
        width: combo_width
    });
    
    var mapLayerPathTextField = new Ext.form.TextField({
        id: 'maplayerpath_tf',
        emptyText: 'Required',
        width: combo_width
    });
    
    var typeComboBox = new Ext.form.ComboBox({
        id: 'type_cb',
        editable: false,
        displayField: 'name',
        valueField: 'name',
        width: combo_width,
        minListWidth: combo_width + 26,
        triggerAction: 'all',
        mode: 'local',
        value: 'Polygon',
        store: new Ext.data.SimpleStore({
            fields: ['name'],
            data: [['Polygon']]
        })
    });
    
    var newUniqueColumnTextField = new Ext.form.TextField({
        id: 'newuniquecolumn_tf',
        emptyText: 'Required',
        width: combo_width
    });
    
    var editUniqueColumnTextField = new Ext.form.TextField({
        id: 'edituniquecolumn_tf',
        emptyText: '',
        width: combo_width
    });
    
    var newNameColumnTextField = new Ext.form.TextField({
        id: 'newnamecolumn_tf',
        emptyText: 'Required',
        width: combo_width
    });
    
    var editNameColumnTextField = new Ext.form.TextField({
        id: 'editnamecolumn_tf',
        emptyText: '',
        width: combo_width
    });
    
    var newLongitudeTextField = new Ext.form.TextField({
        id: 'newlongitude_tf',
        emptyText: 'Required',
        width: combo_width
    });
    
    var editLongitudeTextField = new Ext.form.TextField({
        id: 'editlongitude_tf',
        emptyText: '',
        width: combo_width
    });
    
    var newLatitudeTextField = new Ext.form.TextField({
        id: 'newlatitude_tf',
        emptyText: 'Required',
        width: combo_width
    });
    
    var editLatitudeTextField = new Ext.form.TextField({
        id: 'editlatitude_tf',
        emptyText: '',
        width: combo_width
    });
    
    var newZoomComboBox = new Ext.form.ComboBox({
        id: 'newzoom_cb',
        editable: false,
        emptyText: 'Required',
        displayField: 'value',
        valueField: 'value',
        width: combo_width,
        minListWidth: combo_width + 26,
        triggerAction: 'all',
        mode: 'local',
        value: 7,
        store: new Ext.data.SimpleStore({
            fields: ['value'],
            data: [[5], [6], [7], [8], [9]]
        })
    });
    
    var editZoomComboBox = new Ext.form.ComboBox({
        id: 'editzoom_cb',
        editable: false,
        emptyText: '',
        displayField: 'value',
        valueField: 'value',
        width: combo_width,
        minListWidth: combo_width + 26,
        triggerAction: 'all',
        mode: 'local',
        value: 5,
        store: new Ext.data.SimpleStore({
            fields: ['value'],
            data: [[5], [6], [7], [8], [9]]
        })
    });
    
    var newMapButton = new Ext.Button({
        id: 'newmap_b',
        text: 'Register new map',
        handler: function()
        {
            //var nm = Ext.getCmp('newmap_cb').getValue();
            //var oui = Ext.getCmp('organisationunit_cb').getValue();
    
            Ext.Ajax.request({
                url: path + 'getOrganisationUnitsAtLevel' + type,
                method: 'GET',
                params: { level: 1, format: 'json' },

                success: function( responseObject ) {
                    var oui = Ext.util.JSON.decode( responseObject.responseText ).organisationUnits[0].id;
                    var ouli = Ext.getCmp('organisationunitlevel_cb').getValue();
                    var nn = Ext.getCmp('newname_tf').getValue();
                    var mlp = Ext.getCmp('maplayerpath_tf').getValue();
                    var t = Ext.getCmp('type_cb').getValue();
                    var uc = Ext.getCmp('newuniquecolumn_tf').getValue();
                    var nc = Ext.getCmp('newnamecolumn_tf').getValue();
                    var lon = Ext.getCmp('newlongitude_tf').getValue();
                    var lat = Ext.getCmp('newlatitude_tf').getValue();
                    var zoom = Ext.getCmp('newzoom_cb').getValue();
                     
                    if (!nn || !mlp || !oui || !ouli || !uc || !nc || !lon || !lat) {
                        Ext.messageRed.msg('New map', 'Form is not complete.');
                        return;
                    }
                    
                    if (validateInput(nn) == false) {
                        Ext.messageRed.msg('New map', 'Map name cannot be longer than 25 characters.');
                        return;
                    }
                    
                    Ext.Ajax.request({
                        url: path + 'addOrUpdateMap' + type,
                        method: 'GET',
                        params: { name: nn, mapLayerPath: mlp, type: t, organisationUnitId: oui, organisationUnitLevelId: ouli, uniqueColumn: uc, nameColumn: nc, longitude: lon, latitude: lat, zoom: zoom},

                        success: function( responseObject ) {
                            Ext.messageBlack.msg('New map', 'The map ' + msg_highlight_start + nn + msg_highlight_end + ' was registered.');
                            
                            Ext.getCmp('map_cb').getStore().reload();
                            Ext.getCmp('maps_cb').getStore().reload();
                            Ext.getCmp('editmap_cb').getStore().reload();
                            Ext.getCmp('deletemap_cb').getStore().reload();
                        },
                        failure: function() {
                            alert( 'Status', 'Error while saving data' );
                        }
                    });
                },
                failure: function() {
                    alert( 'Status', 'Error while saving data' );
                }
            });
        }
    });
    
    var editMapButton = new Ext.Button({
        id: 'editmap_b',
        text: 'Save changes',
        handler: function() {
            var en = Ext.getCmp('editname_tf').getValue();
            var em = Ext.getCmp('editmap_cb').getValue();
            var uc = Ext.getCmp('edituniquecolumn_tf').getValue();
            var nc = Ext.getCmp('editnamecolumn_tf').getValue();
            var lon = Ext.getCmp('editlongitude_tf').getValue();
            var lat = Ext.getCmp('editlatitude_tf').getValue();
            var zoom = Ext.getCmp('editzoom_cb').getValue();
            
            if (!en || !em || !uc || !nc || !lon || !lat) {
                Ext.messageRed.msg('New map', 'Form is not complete.');
                return;
            }
            
            if (validateInput(en) == false) {
                Ext.messageRed.msg('New map', 'Map name cannot be longer than 25 characters.');
                return;
            }
           
            Ext.Ajax.request({
                url: path + 'addOrUpdateMap' + type,
                method: 'GET',
                params: { name: en, mapLayerPath: em, uniqueColumn: uc, nameColumn: nc, longitude: lon, latitude: lat, zoom: zoom },

                success: function( responseObject ) {
                    Ext.messageBlack.msg('Edit map', 'The map ' + msg_highlight_start + en + msg_highlight_end + ' was updated.');
                    
                    Ext.getCmp('map_cb').getStore().reload();
                    Ext.getCmp('maps_cb').getStore().reload();
                    Ext.getCmp('editmap_cb').getStore().reload();
                    Ext.getCmp('editmap_cb').reset();
                    Ext.getCmp('deletemap_cb').getStore().reload();
                    Ext.getCmp('deletemap_cb').reset();
                },
                failure: function() {
                    alert( 'Status', 'Error while saving data' );
                }
            });
        }
    });
    
    var deleteMapButton = new Ext.Button({
        id: 'deletemap_b',
        text: 'Delete map',
        handler: function() {
            var mlp = Ext.getCmp('deletemap_cb').getValue();
            
            if (!mlp) {
                Ext.messageRed.msg('Delete map', 'Please select a map.');
                return;
            }
            
            Ext.Ajax.request({
                url: path + 'deleteMap' + type,
                method: 'GET',
                params: { mapLayerPath: mlp },

                success: function( responseObject ) {
                    Ext.messageBlack.msg('Edit map', 'The map ' + msg_highlight_start + mlp + msg_highlight_end + ' was deleted.');
                    
                    Ext.getCmp('map_cb').getStore().reload();
                    Ext.getCmp('maps_cb').getStore().reload();
                    Ext.getCmp('editmap_cb').getStore().reload();
                    Ext.getCmp('editmap_cb').reset();
                    Ext.getCmp('deletemap_cb').getStore().reload();
                    Ext.getCmp('deletemap_cb').reset();
                    Ext.getCmp('mapview_cb').getStore().reload();
                    Ext.getCmp('mapview_cb').reset();
                },
                failure: function() {
                    alert( 'Status', 'Error while saving data' );
                }
            });
        }
    });
    
    var newMapComboBox = new Ext.form.ComboBox({
        id: 'newmap_cb',
        typeAhead: true,
        editable: false,
        valueField: 'level',
        displayField: 'name',
        emptyText: 'Required',
        mode: 'remote',
        forceSelection: true,
        triggerAction: 'all',
        selectOnFocus: true,
        width: combo_width,
        minListWidth: combo_width + 26,
        store: organisationUnitLevelStore,
        listeners: {
            'select': {
                fn: function() {
                    var level = Ext.getCmp('newmap_cb').getValue();
                    organisationUnitStore.baseParams = { level: level, format: 'json' };
                    organisationUnitStore.reload();
                },
                scope: this
            }
        }
    });
    
    var editMapComboBox = new Ext.form.ComboBox({
        id: 'editmap_cb',
        typeAhead: true,
        editable: false,
        valueField: 'mapLayerPath',
        displayField: 'name',
        emptyText: 'Required',
        mode: 'remote',
        forceSelection: true,
        triggerAction: 'all',
        selectOnFocus: true,
        width: combo_width,
        minListWidth: combo_width + 26,
        store: existingMapsStore,
        listeners: {
            'select': {
                fn: function() {
                    var mlp = Ext.getCmp('editmap_cb').getValue();
                    
                    Ext.Ajax.request({
                        url: path + 'getMapByMapLayerPath' + type,
                        method: 'GET',
                        params: { mapLayerPath: mlp, format: 'json' },

                        success: function( responseObject ) {
                            var map = Ext.util.JSON.decode( responseObject.responseText ).map[0];
                            
                            Ext.getCmp('editname_tf').setValue(map.name);
                            Ext.getCmp('edituniquecolumn_tf').setValue(map.uniqueColumn);
                            Ext.getCmp('editnamecolumn_tf').setValue(map.nameColumn);
                            Ext.getCmp('editlongitude_tf').setValue(map.longitude);
                            Ext.getCmp('editlatitude_tf').setValue(map.latitude);
                            Ext.getCmp('editzoom_cb').setValue(map.zoom);
                        },
                        failure: function() {
                            alert( 'Error while retrieving data: getAssignOrganisationUnitData' );
                        } 
                    });
                },
                scope: this
            }
        }
    });
    
    var deleteMapComboBox = new Ext.form.ComboBox({
        xtype: 'combo',
        id: 'deletemap_cb',
        typeAhead: true,
        editable: false,
        valueField: 'mapLayerPath',
        displayField: 'name',
        emptyText: 'Required',
        mode: 'remote',
        forceSelection: true,
        triggerAction: 'all',
        selectOnFocus: true,
        width: combo_width,
        minListWidth: combo_width + 26,
        store: existingMapsStore
    });
    
    var newMapPanel = new Ext.Panel({   
        id: 'newmap_p',
        items:
        [   
//            { html: '<p style="padding-bottom:4px">Map type:</p>' }, typeComboBox, { html: '<br>' },
//            { html: '<p style="padding-bottom:4px">Organisation unit level:</p>' }, newMapComboBox, { html: '<br>' },
//            { html: '<p style="padding-bottom:4px">Organisation unit:</p>' }, multi, { html: '<br>' },
            { html: '<p style="padding-bottom:4px">Organisation unit level:</p>' }, organisationUnitLevelComboBox, { html: '<br>' },
            { html: '<p style="padding-bottom:4px">Map source file:</p>' }, mapLayerPathTextField, { html: '<br>' },
            { html: '<p style="padding-bottom:4px">Map name:</p>' }, newNameTextField, { html: '<br>' },
            { html: '<p style="padding-bottom:4px">Unique column:</p>' }, newUniqueColumnTextField, { html: '<br>' },
            { html: '<p style="padding-bottom:4px">Name column:</p>' }, newNameColumnTextField, { html: '<br>' },
            { html: '<p style="padding-bottom:4px">Longitude:</p>' }, newLongitudeTextField, { html: '<br>' },
            { html: '<p style="padding-bottom:4px">Latitude:</p>' }, newLatitudeTextField, { html: '<br>' },
            { html: '<p style="padding-bottom:4px">Zoom:</p>' }, newZoomComboBox
        ]
    });
    
    var editMapPanel = new Ext.Panel({
        id: 'editmap_p',
        items: [
            { html: '<p style="padding-bottom:4px">Choose a map:</p>' }, editMapComboBox, { html: '<br>' },
            { html: '<p style="padding-bottom:4px">Map name:</p>' }, editNameTextField, { html: '<br>' },
            { html: '<p style="padding-bottom:4px">Unique column:</p>' }, editUniqueColumnTextField, { html: '<br>' },
            { html: '<p style="padding-bottom:4px">Name column:</p>' }, editNameColumnTextField, { html: '<br>' },
            { html: '<p style="padding-bottom:4px">Longitude:</p>' }, editLongitudeTextField, { html: '<br>' },
            { html: '<p style="padding-bottom:4px">Latitude:</p>' }, editLatitudeTextField, { html: '<br>' },
            { html: '<p style="padding-bottom:4px">Zoom:</p>' }, editZoomComboBox
        ]
    });
    
    var deleteMapPanel = new Ext.Panel({
        id: 'deletemap_p',
        items: [
            { html: '<p style="padding-bottom:4px">Choose a map:</p>' }, deleteMapComboBox
        ]
    });

    shapefilePanel = new Ext.Panel({
        id: 'shapefile_p',
        title: '<font style="font-family:tahoma; font-weight:normal; font-size:11px; color:#000000;">Register shapefiles</font>',
        items:
        [
            {
                xtype: 'tabpanel',
                activeTab: 0,
                deferredRender: false,
                plain: true,
                defaults: {layout: 'fit', bodyStyle: 'padding:8px'},
                listeners: {
                    tabchange: function(panel, tab) {
                        var nm_b = Ext.getCmp('newmap_b');
                        var em_b = Ext.getCmp('editmap_b');
                        var dm_b = Ext.getCmp('deletemap_b');
                        
                        if (tab.id == 'map0')
                        { 
                            nm_b.setVisible(true);
                            em_b.setVisible(false);
                            dm_b.setVisible(false);
                        }
                        
                        else if (tab.id == 'map1')
                        {
                            nm_b.setVisible(false);
                            em_b.setVisible(true);
                            dm_b.setVisible(false);
                        }
                        
                        else if (tab.id == 'map2')
                        {
                            nm_b.setVisible(false);
                            em_b.setVisible(false);
                            dm_b.setVisible(true);
                        }
                    }
                },
                items:
                [
                    {
                        title:'New map',
                        id: 'map0',
                        items:
                        [
                            newMapPanel
                        ]
                    },
                    {
                        title:'Edit map',
                        id: 'map1',
                        items:
                        [
                            editMapPanel
                        ]
                    },
                    {
                        title:'Delete map',
                        id: 'map2',
                        items:
                        [
                            deleteMapPanel
                        ]
                    }
                ]
            },
            { html: '<br>' },
            
            newMapButton,
            
            editMapButton,
            
            deleteMapButton
        ]
    });
    
    // LEGEND SET PANEL
    
    var legendSetNameTextField = new Ext.form.TextField({
        id: 'legendsetname_tf',
        emptyText: 'Required',
        width: combo_width
    });
    
    var legendSetMethodComboBox = new Ext.form.ComboBox({
        id: 'legendsetmethod_cb',
        editable: false,
        valueField: 'value',
        displayField: 'text',
        mode: 'local',
        emptyText: 'Required',
        triggerAction: 'all',
        width: combo_width,
        minListWidth: combo_width + 26,
        store: new Ext.data.SimpleStore({
            fields: ['value', 'text'],
            data: [[2, 'Distributed values'], [1, 'Equal intervals']]
        })
    });
    
    var legendSetClassesComboBox = new Ext.form.ComboBox({
        id: 'legendsetclasses_cb',
        editable: false,
        valueField: 'value',
        displayField: 'value',
        mode: 'local',
        emptyText: 'Required',
        triggerAction: 'all',
        width: combo_width,
        minListWidth: combo_width + 26,
        store: new Ext.data.SimpleStore({
            fields: ['value'],
            data: [[1], [2], [3], [4], [5], [6], [7], [8]]
        })
    });
    
    var legendSetLowColorColorPalette = new Ext.ux.ColorField({
        id: 'legendsetlowcolor_cp',
        allowBlank: false,
        width: combo_width,
        minListWidth: combo_width + 26,
        value: "#FFFF00"
    });
    
    var legendSetHighColorColorPalette = new Ext.ux.ColorField({
        id: 'legendsethighcolor_cp',
        allowBlank: false,
        width: combo_width,
        minListWidth: combo_width + 26,
        value: "#FF0000"
    });

    var legendSetIndicatorStore = new Ext.data.JsonStore({
        url: path + 'getAllIndicators' + type,
        root: 'indicators',
        fields: ['id', 'name', 'shortName'],
        sortInfo: { field: 'name', direction: 'ASC' },
        autoLoad: true
    });
    
    var legendSetIndicatorMultiSelect = new Ext.ux.Multiselect({
        id: 'legendsetindicator_ms',
        dataFields: ['id', 'name', 'shortName'], 
        valueField: 'id',
        displayField: 'shortName',
        width: gridpanel_width - 25,
        height: getMultiSelectHeight(),
        store: legendSetIndicatorStore
    });
    
    var legendSetStore = new Ext.data.JsonStore({
        url: path + 'getAllMapLegendSets' + type,
        root: 'mapLegendSets',
        fields: ['id', 'name'],
        sortInfo: { field: 'name', direction: 'ASC' },
        autoLoad: true
    });
    
    var legendSetComboBox = new Ext.form.ComboBox({
        id: 'legendset_cb',
        typeAhead: true,
        editable: false,
        valueField: 'id',
        displayField: 'name',
        mode: 'remote',
        forceSelection: true,
        triggerAction: 'all',
        emptyText: 'Required',
        selectOnFocus: true,
        width: combo_width,
        minListWidth: combo_width + 26,
        store: legendSetStore
    });
    
    var newLegendSetButton = new Ext.Button({
        id: 'newlegendset_b',
        text: 'Register new legend set',
        handler: function() {
            var ln = Ext.getCmp('legendsetname_tf').getValue();
//            var lm = Ext.getCmp('legendsetmethod_cb').getValue();
            var lc = Ext.getCmp('legendsetclasses_cb').getValue();            
            var llc = Ext.getCmp('legendsetlowcolor_cp').getValue();
            var lhc = Ext.getCmp('legendsethighcolor_cp').getValue();
//            var li = Ext.getCmp('legendsetindicator_cb').getValue();
            var lims = Ext.getCmp('legendsetindicator_ms').getValue();
            
            if (!lc || !ln || !lims) {
                Ext.messageRed.msg('New legend set', 'Form is not complete.');
                return;
            }
            
            if (validateInput(ln) == false) {
                Ext.messageRed.msg('New legend set', 'Legend set name cannot be longer than 25 characters.');
                return;
            }
            
            var array = new Array();
            array = lims.split(',');
            var params = '?indicators=' + array[0];
            
            for (var i = 1; i < array.length; i++) {
                array[i] = '&indicators=' + array[i];
                params += array[i];
            }
            
            Ext.Ajax.request({
                url: path + 'addOrUpdateMapLegendSet.action' + params,
                method: 'POST',
                params: { name: ln, method: 2, classes: lc, colorLow: llc, colorHigh: lhc },

                success: function( responseObject ) {
                    Ext.messageBlack.msg('New legend set', 'The legend set ' + msg_highlight_start + ln + msg_highlight_end + ' was registered.');
                    Ext.getCmp('legendset_cb').getStore().reload();
                },
                failure: function() {
                    alert( 'Status', 'Error while saving data' );
                }
            });
        }
    });
    
    var deleteLegendSetButton = new Ext.Button({
        id: 'deletelegendset_b',
        text: 'Delete legend set',
        handler: function() {
            var ls = Ext.getCmp('legendset_cb').getValue();
            var lsrw = Ext.getCmp('legendset_cb').getRawValue();
            
            if (!ls) {
                Ext.messageRed.msg('Delete legend set', 'Please select a legend set.');
                return;
            }
            
            Ext.Ajax.request({
                url: path + 'deleteMapLegendSet' + type,
                method: 'GET',
                params: { id: ls },

                success: function( responseObject ) {
                    Ext.messageBlack.msg('Delete legend set', 'The legend set ' + msg_highlight_start + lsrw + msg_highlight_end + ' was deleted.');
                    
                    Ext.getCmp('legendset_cb').getStore().reload();
                    Ext.getCmp('legendset_cb').reset();
                },
                failure: function() {
                    alert( 'Status', 'Error while saving data' );
                }
            });
        }
    });
    
    var newLegendSetPanel = new Ext.Panel({   
        id: 'newlegendset_p',
        items:
        [   
            { html: '<p style="padding-bottom:4px">Legend set name:</p>' }, legendSetNameTextField, { html: '<br>' },
//            { html: '<p style="padding-bottom:4px">Method:</p>' }, legendSetMethodComboBox, { html: '<br>' },
            { html: '<p style="padding-bottom:4px">Classes:</p>' }, legendSetClassesComboBox, { html: '<br>' },
            { html: '<p style="padding-bottom:4px">Lowest value color:</p>' }, legendSetLowColorColorPalette, { html: '<br>' },
            { html: '<p style="padding-bottom:4px">Highest value color:</p>' }, legendSetHighColorColorPalette, { html: '<br>' },
//            { html: '<p style="padding-bottom:4px">Indicator group:</p>' }, legendSetIndicatorGroupComboBox, { html: '<br>' },
//            { html: '<p style="padding-bottom:4px">Indicator:</p>' }, legendSetIndicatorComboBox
            { html: '<p style="padding-bottom:4px">Indicators:</p>' }, legendSetIndicatorMultiSelect
        ]
    });
    
    var deleteLegendSetPanel = new Ext.Panel({   
        id: 'deletelegendset_p',
        items:
        [   
            { html: '<p style="padding-bottom:4px">Legend set:</p>' }, legendSetComboBox
        ]
    });

    var legendsetPanel = new Ext.Panel({
        id: 'legendset_p',
        title: '<font style="font-family:tahoma; font-weight:normal; font-size:11px; color:#133a75;">Map legend sets</font>',
        items:
        [
            {
                xtype: 'tabpanel',
                activeTab: 0,
                deferredRender: false,
                plain: true,
                defaults: {layout: 'fit', bodyStyle: 'padding:8px'},
                listeners: {
                    tabchange: function(panel, tab) {
                        var nl_b = Ext.getCmp('newlegendset_b');
                        var dl_b = Ext.getCmp('deletelegendset_b');
                        
                        if (tab.id == 'legendset0') { 
                            nl_b.setVisible(true);
                            dl_b.setVisible(false);
                        }
                        else if (tab.id == 'legendset1') {
                            nl_b.setVisible(false);
                            dl_b.setVisible(true);
                        }
                    }
                },
                items:
                [
                    {
                        title:'New legend set',
                        id: 'legendset0',
                        items:
                        [
                            newLegendSetPanel
                        ]
                    },
                    {
                        title:'Delete legend set',
                        id: 'legendset1',
                        items:
                        [
                            deleteLegendSetPanel
                        ]
                    }
                ]
            },
            { html: '<br>' },
            
            newLegendSetButton,
            
            deleteLegendSetButton
        ]
    });
    
    // VIEW PANEL
    
    var viewNameTextField = new Ext.form.TextField({
        id: 'viewname_tf',
        emptyText: 'Required',
        width: combo_width
    });
    
    var viewStore = new Ext.data.JsonStore({
        url: path + 'getAllMapViews' + type,
        root: 'mapViews',
        fields: ['id', 'name'],
        sortInfo: { field: 'name', direction: 'ASC' },
        autoLoad: true
    });
    
    var viewComboBox = new Ext.form.ComboBox({
        id: 'view_cb',
        typeAhead: true,
        editable: false,
        valueField: 'id',
        displayField: 'name',
        mode: 'remote',
        forceSelection: true,
        triggerAction: 'all',
        emptyText: 'Required',
        selectOnFocus: true,
        width: combo_width,
        minListWidth: combo_width + 26,
        store: viewStore
    });
    
    var view2ComboBox = new Ext.form.ComboBox({
        id: 'view2_cb',
        typeAhead: true,
        editable: false,
        valueField: 'id',
        displayField: 'name',
        mode: 'remote',
        forceSelection: true,
        triggerAction: 'all',
        emptyText: 'Required',
        selectOnFocus: true,
        width: combo_width,
        minListWidth: combo_width + 26,
        store: viewStore
    });
    
    var newViewButton = new Ext.Button({
        id: 'newview_b',
        text: 'Register new view',
        handler: function() {
            var vn = Ext.getCmp('viewname_tf').getValue();
            var ig = Ext.getCmp('indicatorgroup_cb').getValue();
            var i = Ext.getCmp('indicator_cb').getValue();
            var pt = Ext.getCmp('periodtype_cb').getValue();
            var p = Ext.getCmp('period_cb').getValue();
            var mst = MAPSOURCE;
            var ms = Ext.getCmp('map_cb').getValue();
            var c = Ext.getCmp('numClasses').getValue();
            var ca = Ext.getCmp('colorA_cf').getValue();
            var cb = Ext.getCmp('colorB_cf').getValue();
            
            if (!vn || !ig || !i || !pt || !p || !mst || !ms || !c ) {
                Ext.messageRed.msg('New map view', 'Map view form is not complete.');
                return;
            }
            
            if (validateInput(vn) == false) {
                Ext.messageRed.msg('New map view', 'Map view name cannot be longer than 25 characters.');
                return;
            }
            
            Ext.Ajax.request({
                url: path + 'addOrUpdateMapView' + type,
                method: 'POST',
                params: { name: vn, indicatorGroupId: ig, indicatorId: i, periodTypeId: pt, periodId: p, mapSourceType: mst, mapSource: ms, method: 2, classes: c, colorLow: ca, colorHigh: cb },

                success: function( responseObject ) {
                    Ext.messageBlack.msg('New map view', 'The view ' + msg_highlight_start + vn + msg_highlight_end + ' was registered.');
                    Ext.getCmp('view_cb').getStore().reload();
                    Ext.getCmp('mapview_cb').getStore().reload();
                },
                failure: function() {
                    alert( 'Status', 'Error while saving data' );
                }
            });
        }
    });
    
    var deleteViewButton = new Ext.Button({
        id: 'deleteview_b',
        text: 'Delete view',
        handler: function() {
            var v = Ext.getCmp('view_cb').getValue();
            
            if (!v) {
                Ext.messageRed.msg('Delete map view', 'Please select a map view.');
                return;
            }
            
            Ext.Ajax.request({
                url: path + 'deleteMapView' + type,
                method: 'POST',
                params: { id: v },

                success: function( responseObject ) {
                    Ext.messageBlack.msg('Delete map view', 'The map view ' + v + ' was deleted.');
                    Ext.getCmp('view_cb').getStore().reload();
                    Ext.getCmp('view_cb').reset();
                    Ext.getCmp('mapview_cb').getStore().reload();
                },
                failure: function() {
                    alert( 'Status', 'Error while saving data' );
                }
            });
        }
    });
    
    var dashboardViewButton = new Ext.Button({
        id: 'dashboardview_b',
        text: 'Add view to DHIS2 dashboard',
        handler: function() {
            var v2 = Ext.getCmp('view2_cb').getValue();
            
            if (!v2) {
                Ext.messageRed.msg('Dashboard map view', 'Please select a map view.');
                return;
            }
            
            Ext.Ajax.request({
                url: path + 'addMapViewToDashboard' + type,
                method: 'POST',
                params: { id: v2 },

                success: function( responseObject ) {
                    Ext.messageBlack.msg('Dashboard map view', 'The view ' + v + ' was added to dashboard.');
                    Ext.getCmp('view_cb').getStore().reload();
                    Ext.getCmp('view_cb').reset();
                    Ext.getCmp('mapview_cb').getStore().reload();
                },
                failure: function() {
                    alert( 'Status', 'Error while saving data' );
                }
            });
        }
    });
    
    var newViewPanel = new Ext.Panel({   
        id: 'newview_p',
        items:
        [
            { html: 'Saving current thematic map selection.' }, { html: '<br>' },
            { html: '<p style="padding-bottom:4px">Name:</p>' }, viewNameTextField
        ]
    });
    
    var deleteViewPanel = new Ext.Panel({   
        id: 'deleteview_p',
        items:
        [   
            { html: '<p style="padding-bottom:4px">View:</p>' }, viewComboBox
        ]
    });
    
    var dashboardViewPanel = new Ext.Panel({   
        id: 'dashboardview_p',
        items:
        [   
            { html: '<p style="padding-bottom:4px">View:</p>' }, view2ComboBox
        ]
    });
    
    var viewPanel = new Ext.Panel({
        id: 'view_p',
        title: '<font style="font-family:tahoma; font-weight:normal; font-size:11px; color:#133a75;">Map views</font>',
        items:
        [
            {
                xtype: 'tabpanel',
                activeTab: 0,
                deferredRender: false,
                plain: true,
                defaults: {layout: 'fit', bodyStyle: 'padding:8px'},
                listeners: {
                    tabchange: function(panel, tab)
                    {
                        var nv_b = Ext.getCmp('newview_b');
                        var dv_b = Ext.getCmp('deleteview_b');
                        var dbv_b = Ext.getCmp('dashboardview_b');
                        
                        if (tab.id == 'view0')
                        { 
                            nv_b.setVisible(true);
                            dv_b.setVisible(false);
                            dbv_b.setVisible(false);
                        }
                        
                        else if (tab.id == 'view1')
                        {
                            nv_b.setVisible(false);
                            dv_b.setVisible(true);
                            dbv_b.setVisible(false);
                        }
                        
                        else if (tab.id == 'view2')
                        {
                            nv_b.setVisible(false);
                            dv_b.setVisible(false);
                            dbv_b.setVisible(true);
                        }
                    }
                },
                items:
                [
                    {
                        title:'New view',
                        id: 'view0',
                        items:
                        [
                            newViewPanel
                        ]
                    },
                    
                    {
                        title:'Delete view',
                        id: 'view1',
                        items:
                        [
                            deleteViewPanel
                        ]
                    },
                    
                    {
                        title:'Dashboard view',
                        id: 'view2',
                        items:
                        [
                            dashboardViewPanel
                        ]
                    }
                ]
            },
            
            { html: '<br>' },
            
            newViewButton,
            
            deleteViewButton,
            
            dashboardViewButton
        ]
    });
    
    // MAP LAYER PANEL
    
    var mapLayerNameTextField = new Ext.form.TextField({
        id: 'maplayername_tf',
        emptyText: 'Required',
        width: combo_width
    });
    
    var mapLayerMapSourceFileTextField = new Ext.form.TextField({
        id: 'maplayermapsourcefile_tf',
        emptyText: 'Required',
        width: combo_width
    });
    
    var mapLayerStore = new Ext.data.JsonStore({
        url: path + 'getAllMapLayers' + type,
        root: 'mapLayers',
        fields: ['id', 'name'],
        sortInfo: { field: 'name', direction: 'ASC' },
        autoLoad: true
    });
    
    var mapLayerComboBox = new Ext.form.ComboBox({
        id: 'maplayer_cb',
        typeAhead: true,
        editable: false,
        valueField: 'id',
        displayField: 'name',
        mode: 'remote',
        forceSelection: true,
        triggerAction: 'all',
        emptyText: 'Required',
        selectOnFocus: true,
        width: combo_width,
        minListWidth: combo_width + 26,
        store: mapLayerStore
    });
    
    var newMapLayerButton = new Ext.Button({
        id: 'newmaplayer_b',
        text: 'Register new map layer',
        handler: function() {
            var mln = Ext.getCmp('maplayername_tf').getRawValue();
            var mlmsf = Ext.getCmp('maplayermapsourcefile_tf').getValue();
            
            if (!mln || !mlmsf ) {
                Ext.messageRed.msg('New map layer', 'Map layer form is not complete.');
                return;
            }
            
            if (validateInput(mln) == false) {
                Ext.messageRed.msg('New map layer', 'Map layer name cannot be longer than 25 characters.');
                return;
            }
            
            Ext.Ajax.request({
                url: path + 'addOrUpdateMapLayer' + type,
                method: 'POST',
                params: { name: mln, mapSource: mlmsf },

                success: function( responseObject ) {
                    Ext.messageBlack.msg('New map layer', 'The map layer ' + msg_highlight_start + mln + msg_highlight_end + ' was registered.');
                    Ext.getCmp('maplayer_cb').getStore().reload();
                },
                failure: function() {
                    alert( 'Status', 'Error while saving data' );
                }
            });
        }
    });
    
    var deleteMapLayerButton = new Ext.Button({
        id: 'deletemaplayer_b',
        text: 'Delete map layer',
        handler: function() {
            var ml = Ext.getCmp('maplayer_cb').getValue();
            
            if (!ml) {
                Ext.messageRed.msg('Delete map layer', 'Please select a map layer.');
                return;
            }
            
            Ext.Ajax.request({
                url: path + 'deleteMapLayer' + type,
                method: 'POST',
                params: { id: ml },

                success: function( responseObject ) {
                    Ext.messageBlack.msg('Delete map layer', 'The map layer ' + ml + ' was deleted.');
                    Ext.getCmp('maplayer_cb').getStore().reload();
                    Ext.getCmp('maplayer_cb').reset();
                },
                failure: function() {
                    alert( 'Status', 'Error while saving data' );
                }
            });
        }
    });
    
    var newMapLayerPanel = new Ext.Panel({   
        id: 'newmaplayer_p',
        items:
        [
            { html: '<p style="padding-bottom:4px">Name:</p>' }, mapLayerNameTextField, { html: '<br>' }, 
            { html: '<p style="padding-bottom:4px">Map source file:</p>' }, mapLayerMapSourceFileTextField
        ]
    });
    
    var deleteMapLayerPanel = new Ext.Panel({   
        id: 'deletemaplayer_p',
        items:
        [   
            { html: '<p style="padding-bottom:4px">Map layer:</p>' }, mapLayerComboBox
        ]
    });
    
    var mapLayerPanel = new Ext.Panel({
        id: 'maplayer_p',
        title: '<font style="font-family:tahoma; font-weight:normal; font-size:11px; color:black;">Map layers</font>',
        items:
        [
            {
                xtype: 'tabpanel',
                activeTab: 0,
                deferredRender: false,
                plain: true,
                defaults: {layout: 'fit', bodyStyle: 'padding:8px'},
                listeners: {
                    tabchange: function(panel, tab)
                    {
                        var nml_b = Ext.getCmp('newmaplayer_b');
                        var dml_b = Ext.getCmp('deletemaplayer_b');
                        
                        if (tab.id == 'maplayer0') { 
                            nml_b.setVisible(true);
                            dml_b.setVisible(false);
                        }
                        else if (tab.id == 'maplayer1') {
                            nml_b.setVisible(false);
                            dml_b.setVisible(true);
                        }
                    }
                },
                items:
                [
                    {
                        title:'New map layer',
                        id: 'maplayer0',
                        items:
                        [
                            newMapLayerPanel
                        ]
                    },
                    {
                        title:'Delete map layer',
                        id: 'maplayer1',
                        items:
                        [
                            deleteMapLayerPanel
                        ]
                    }
                ]
            },
            
            { html: '<br>' },
            
            newMapLayerButton,
            
            deleteMapLayerButton
        ]
    });
    
    // ADMIN PANEL
    
    var adminPanel = new Ext.form.FormPanel({
        id: 'admin_p',
        title: '<font style="font-family:tahoma; font-weight:normal; font-size:11px; color:#000000">Admin</font>',
        items:
        [   
            {
                xtype: 'checkbox',
                id: 'register_chb',
                fieldLabel: 'Admin panels',
                isFormField: true,
                listeners: {
                    'check': {
                        fn: function(checkbox,checked) {
                            if (checked) {
                                mapping.show();
                                shapefilePanel.show();
                                mapLayerPanel.show();
                                Ext.getCmp('west').doLayout();
                            }
                            else {
                                mapping.hide();
                                shapefilePanel.hide();
                                mapLayerPanel.hide();
                                Ext.getCmp('west').doLayout();
                            }
                        },
                        scope: this
                    }
                }
            },
            {
                xtype: 'combo',
                fieldLabel: 'Map source',
                id: 'mapsource_cb',
                editable: false,
                valueField: 'id',
                displayField: 'text',
                mode: 'local',
                emptyText: 'Required',
                triggerAction: 'all',
                width: 133,
                minListWidth: combo_width,
                store: new Ext.data.SimpleStore({
                    fields: ['id', 'text'],
                    data: [['database', 'DHIS database'], ['shapefile', 'Shapefile']]
                }),
                listeners:{
                    'select': {
                        fn: function() {
                            var msv = Ext.getCmp('mapsource_cb').getValue();
                            var msrw = Ext.getCmp('mapsource_cb').getRawValue();
                            
                            Ext.Ajax.request({
                                url: path + 'getMapSourceTypeUserSetting' + type,
                                method: 'POST',

                                success: function( responseObject ) {
                                    if (Ext.util.JSON.decode(responseObject.responseText).mapSource == msv) {
                                        Ext.messageRed.msg('Map source', msg_highlight_start + msrw + msg_highlight_end + ' is already selected.');
                                    }
                                    else {
                                        Ext.Ajax.request({
                                            url: path + 'setMapSourceTypeUserSetting' + type,
                                            method: 'POST',
                                            params: { mapSourceType: msv },

                                            success: function( responseObject ) {
                                                Ext.messageBlack.msg('Map source', msg_highlight_start + msrw + msg_highlight_end + ' is saved as map source.');

                                                MAPSOURCE = msv;
                                                
                                                Ext.getCmp('map_cb').getStore().reload();
                                                Ext.getCmp('maps_cb').getStore().reload();
                                                Ext.getCmp('mapview_cb').getStore().reload();

                                                Ext.getCmp('map_cb').reset();
                                                Ext.getCmp('mapview_cb').reset();
                                                
                                                if (MAPSOURCE == 'shapefile') {
                                                    Ext.getCmp('register_chb').enable();
                                                }
                                                else if (MAPSOURCE == 'database') {
                                                    Ext.getCmp('register_chb').disable();
                                                }
                                            },
                                            failure: function() {
                                                alert( 'Status', 'Error while saving data' );
                                            }
                                        });
                                    }
                                },
                                failure: function() {
                                    alert( 'Status', 'Error while saving data' );
                                }
                            });
                        }
                    }
                }
            }
        ],
        listeners: {
            expand: {
                fn: function() {
                    Ext.getCmp('mapsource_cb').setValue(MAPSOURCE);
                    
                    if (MAPSOURCE == 'shapefile') {
                        Ext.getCmp('register_chb').enable();
                    }
                    else if (MAPSOURCE == 'database') {
                        Ext.getCmp('register_chb').disable();
                    }
                }
            }
        }
    });
    
    // WIDGETS
    
    choropleth = new mapfish.widgets.geostat.Choropleth({
        id: 'choropleth',
        map: map,
        layer: choroplethLayer,
        title: '<font style="font-family:tahoma; font-weight:normal; font-size:11px; color:#133a75;">Thematic map</font>',
        nameAttribute: 'NAME',
        indicators: [['value', 'Indicator']],
        url: INIT_URL,
        featureSelection: false,
        loadMask: {msg: 'Loading shapefile...', msgCls: 'x-mask-loading'},
        legendDiv: 'choroplethLegend',
        defaults: {width: 130},
        listeners: {
            expand: {
                fn: function() {
                    choroplethLayer.setVisibility(false);
                    choropleth.classify(false);
                    
                    ACTIVEPANEL = 'choropleth';
                }
            }
        }
    });
    
    mapping = new mapfish.widgets.geostat.Mapping({
        id: 'mapping',
        map: map,
        layer: choroplethLayer,
        title: '<font style="font-family:tahoma; font-weight:normal; font-size:11px; color:#000000;">Assign organisation units</font>',
        nameAttribute: 'NAME',
        indicators: [['value', 'Indicator']],
        url: INIT_URL,
        featureSelection: false,
        loadMask: {msg: 'Loading shapefile...', msgCls: 'x-mask-loading'},
        legendDiv: 'choroplethLegend',
        defaults: {width: 130},
        listeners: {
            expand: {
                fn: function() {
                    choroplethLayer.setVisibility(false);
                    
                    ACTIVEPANEL = 'mapping';
                }
            }
        }
    });
    
    static1 = new mapfish.widgets.geostat.Static({
        id: 'static1',
        map: map,
        layer: static1Layer,
        title: STATIC1_LAYERNAME,
        nameAttribute: 'NAME',
        indicators: [['value', 'Indicator']],
        url: INIT_URL,
        featureSelection: false,
        loadMask: {msg: 'Loading shapefile...', msgCls: 'x-mask-loading'},
        legendDiv: 'choroplethLegend',
        defaults: {width: 130},
        listeners: {
            expand: {
                fn: function() {}
            }
        }
    });
    
    static1.hide();
    mapping.hide();
    shapefilePanel.hide();
    mapLayerPanel.hide();
    
    var mapPanel = new GeoExt.MapPanel({
        region: 'center',
        id: 'center',
        height: 1000,
        width: 1000,
        map: map,
        title: '',
        zoom: 3
    });
    
    viewport = new Ext.Viewport({
        id: 'viewport',
        layout: 'border',
        margins: '0 0 5 0',
        items:
        [
            new Ext.BoxComponent(
            {
                // raw
                region: 'north',
                id: 'north',
                el: 'north',
                height: north_height
            }),
            //{
            //    region: 'south',
            //    contentEl: 'south',
            //    id: 'south-panel',
            //    split: true,
            //    height: 70,
            //    minSize: 50,
            //    maxSize: 200,
            //    collapsible: true,
            //    collapsed: true,
            //    title: 'Status',
            //    margins: '0 5 5 5',
            //    bodyStyle: 'padding:5px; font-family:tahoma; font-size:12px',
            //},
            {
                region: 'east',
                id: 'east',
                title: '',
                width: 200,
                collapsible: true,
                margins: '0 5 0 5',
                defaults: {
                    border: true,
                    frame: true
                },
                layout: 'anchor',
                items:
                [
                    {
                        title: 'Map layers',
                        id: 'layertree',
                        autoHeight: true,
                        xtype: 'layertree',
                        map: map,
                        anchor: '100%'
                    },
                    {
                        title: 'Overview',
                        autoHeight: true,
                        html:'<div id="overviewmap"></div>',
                        anchor: '100%'
                    },
                    {
                        title: 'Cursor position',
                        height: 65,
                        contentEl: 'position',
                        anchor: '100%'
                    },
                    {
                        title: 'Map legend',
                        minHeight: 65,
                        autoHeight: true,
                        contentEl: 'legend',
                        anchor: '100%'
                    }
                ]
            },
            {
                region: 'west',
                id: 'west',
                //title: '',
                split: true,
                collapsible: true,
                width: west_width,
                minSize: 175,
                maxSize: 500,
                margins: '0 0 0 5',
                layout: 'accordion',
                defaults: {
                    border: true,
                    frame: true
                },
                items:
                [
                    choropleth,
                    viewPanel,
                    legendsetPanel,
                    shapefilePanel,
                    mapping,
                    mapLayerPanel,
                    adminPanel,
                    static1//,
/*                    {
                        xtype: 'print-simple',
                        title: 'Print single page',
                        bodyStyle: 'padding: 7px;',
                        formConfig: {
                            labelWidth: 65,
                            defaults: {
                                width: 140,
                                listWidth: 140
                            },
                            items: [
                                {
                                    xtype: 'textfield',
                                    fieldLabel: 'Title',
                                    name: 'mapTitle',
                                    value: 'Map title'
                                },
                                {
                                    xtype: 'textarea',
                                    fieldLabel: 'Comments',
                                    name: 'comment',
                                    height: 100,
                                    value: 'Some comments'
                                }
                            ]
                        },
                        border: false,
                        map: map,
                        configUrl: printConfigUrl
                    },
                    {
                      xtype: 'print-multi',
                      title: 'Print multi page',
                      formConfig: {
                        labelWidth: 65,
                        bodyStyle: 'padding: 7px;',
                        defaults: {
                          width: 140,
                          listWidth: 140
                        //},
                        //items: [
                        //  {
                        //    xtype: 'textfield',
                        //    fieldLabel: 'Title',
                        //    name: 'title',
                        //    value: 'Map title'
                        //  }
                        //]
                      },
                      columns: [
                          {
                              header: OpenLayers.Lang.translate('mf.print.mapTitle'),
                              dataIndex: 'mapTitle',
                              editor: new Ext.form.TextField()
                          },
                          {
                              header: OpenLayers.Lang.translate('mf.print.comment'),
                              dataIndex: 'comment',
                              editor: new Ext.form.TextField()
                          }
                      ],
                      border: false,
                      map: map,
                      configUrl: printConfigUrl
                    }*/
                ]
            },
            {
                xtype: 'gx_mappanel',
                region: 'center',
                id: 'center',
                height: 1000,
                width: 1000,
                map: map,
                title: '',
                zoom: 3
            }
        ]
    });
    
    map.addControl(new OpenLayers.Control.MousePosition({
        displayClass: "void", 
        div: $('mouseposition'), 
        prefix: 'x: ',
        separator: '<br/>y: '
    }));

    map.addControl(new OpenLayers.Control.OverviewMap({div: $('overviewmap')}));
    
    map.addControl(new OpenLayers.Control.ZoomBox());
    
    map.events.on({
        changelayer: function(e) {
            if (e.property == 'visibility' && e.layer != choroplethLayer) {
                if (static1Layer.visibility) {
                    selectFeatureChoropleth.deactivate();
                    
                    if (!STATIC1LOADED) {
                        STATIC1LOADED = true;
                        static1.setUrl(STATIC1_URL);
                    }
                }
                else {
                    selectFeatureChoropleth.activate();
                }
            }
        }
    });  
    
    Ext.get('loading').fadeOut({remove: true});
});

// SELECT FEATURES

function onHoverSelectChoropleth(feature) {
    if (MAPDATA != null) {
        var center_panel = Ext.getCmp('center');
        var south_panel = Ext.getCmp('south-panel');

        var height = 230;
        var collapsed = 48;
        var padding_x = 15;
        var padding_y = 22;

        var x = center_panel.x + padding_x;
        var y = south_panel.y + collapsed - height  - padding_y;

        popup_feature = new Ext.Window({
            title: 'Organisation unit',
            width: 190,
            height: height,
            layout: 'fit',
            plain: true,
            bodyStyle: 'padding:5px',
            x: x,
            y: y
        });    

        style = '<p style="margin-top: 5px; padding-left:5px;">';
        space = '&nbsp;&nbsp;';
        bs = '<b>';
        be = '</b>';
        lf = '<br>';
        pe = '</p>';

        var html = style + feature.attributes[MAPDATA.nameColumn] + pe;
        html += style + bs + 'Value:' + be + space + feature.attributes.value + pe;
        
        popup_feature.html = html;
        popup_feature.show();
    }
}

function onHoverUnselectChoropleth(feature) {
    if (MAPDATA != null) {
        popup_feature.hide();
    }
}

function onClickSelectChoropleth(feature) {
    if (ACTIVEPANEL == 'mapping') {
        if (!Ext.getCmp('grid_gp').getSelectionModel().getSelected()) {
            Ext.messageRed.msg('Assign organisation units', 'Please select an organisation unit in the list.');
            return;
        }
        
        var selected = Ext.getCmp('grid_gp').getSelectionModel().getSelected();
        var organisationUnitId = selected.data['organisationUnitId'];
        var organisationUnit = selected.data['organisationUnit'];
        
        var uniqueColumn = MAPDATA.uniqueColumn;
        var nameColumn = MAPDATA.nameColumn;
        var mlp = MAPDATA.mapLayerPath;
        var featureId = feature.attributes[uniqueColumn];
        var name = feature.attributes[nameColumn];

        Ext.Ajax.request({
            url: path + 'addOrUpdateMapOrganisationUnitRelation' + type,
            method: 'GET',
            params: { mapLayerPath: mlp, organisationUnitId: organisationUnitId, featureId: featureId },

            success: function( responseObject ) {
                Ext.messageBlack.msg('Assign organisation units', msg_highlight_start + organisationUnit + msg_highlight_end + ' (database) assigned to ' + msg_highlight_start + name + msg_highlight_end + ' (shapefile).');
                
                Ext.getCmp('grid_gp').getStore().reload();
                loadMapData('assignment');
            },
            failure: function() {
                alert( 'Status', 'Error while retrieving data' );
            } 
        });
        
        popup_feature.hide();
    }
}

function onClickUnselectChoropleth(feature) {}


// MAP DATA

function loadMapData(redirect) {
    Ext.Ajax.request({
        url: path + 'getMapByMapLayerPath' + type,
        method: 'POST',
        params: { mapLayerPath: URL, format: 'json' },

        success: function( responseObject ) {
            MAPDATA = Ext.util.JSON.decode(responseObject.responseText).map[0];
            
            if (MAPSOURCE == 'database') {
                MAPDATA.name = Ext.getCmp('map_cb').getRawValue();
                MAPDATA.organisationUnit = 'Country';
                MAPDATA.organisationUnitLevel = Ext.getCmp('map_cb').getValue();
                MAPDATA.unqiueColumn = 'name';
                MAPDATA.nameColumn = 'name';
                MAPDATA.longitude = COUNTRY_LONGITUDE;
                MAPDATA.latitude = COUNTRY_LATITUDE;
                MAPDATA.zoom = COUNTRY_ZOOM;
            }
            else if (MAPSOURCE == 'shapefile') {
                MAPDATA.organisationUnitLevel = parseFloat(MAPDATA.organisationUnitLevel);
                MAPDATA.longitude = parseFloat(MAPDATA.longitude);
                MAPDATA.latitude = parseFloat(MAPDATA.latitude);
                MAPDATA.zoom = parseFloat(MAPDATA.zoom);
            }
            
            map.setCenter(new OpenLayers.LonLat(MAPDATA.longitude, MAPDATA.latitude), MAPDATA.zoom);

            if (redirect == 'choropleth') {
                getChoroplethData(); }
            else if (redirect == 'point') {
                getPointData(); }
            else if (redirect == 'assignment') {
                getAssignOrganisationUnitData(); }
            else if (redirect == 'auto-assignment') {
                getAutoAssignOrganisationUnitData(); }
        },
        failure: function() {
            alert( 'Error while retrieving map data: loadMapData' );
        } 
    });
}

// CHOROPLETH
function getChoroplethData() {
    var indicatorId = Ext.getCmp('indicator_cb').getValue();
    var periodId = Ext.getCmp('period_cb').getValue();
    var level = MAPDATA.organisationUnitLevel;

    Ext.Ajax.request({
        url: path + 'getMapValues' + type,
        method: 'POST',
        params: { indicatorId: indicatorId, periodId: periodId, level: level, format: 'json' },

        success: function( responseObject ) {
            dataReceivedChoropleth( responseObject.responseText );
        },
        failure: function() {
            alert( 'Status', 'Error while retrieving data' );
        } 
    });
}

function dataReceivedChoropleth( responseText ) {
    var layers = this.myMap.getLayersByName(CHOROPLETH_LAYERNAME);
    var features = layers[0].features;
    
    var mapvalues = Ext.util.JSON.decode(responseText).mapvalues;
    
    if (MAPSOURCE == 'database') {
        for (var i=0; i < features.length; i++) {
            for (var j=0; j < mapvalues.length; j++) {
                if (features[i].attributes.value == null) {
                    features[i].attributes.value = 0;
                }

                if (features[i].attributes.name == mapvalues[j].orgUnit) {
                    features[i].attributes.value = parseFloat(mapvalues[j].value);
                }
            }
        }
        
        var options = {};
        
        // hidden
        choropleth.indicator = 'value';
        choropleth.indicatorText = 'Indicator';
        options.indicator = choropleth.indicator;
        
        options.method = Ext.getCmp('method').getValue();
        options.numClasses = Ext.getCmp('numClasses').getValue();
        options.colors = choropleth.getColors();

        choropleth.coreComp.updateOptions(options);
        choropleth.coreComp.applyClassification();
        choropleth.classificationApplied = true;
        
        MASK.hide();
    }
    else {
        var mlp = MAPDATA.mapLayerPath;
        var uniqueColumn = MAPDATA.uniqueColumn;
        
        Ext.Ajax.request({
            url: path + 'getAvailableMapOrganisationUnitRelations' + type,
            method: 'POST',
            params: { mapLayerPath: mlp, format: 'json' },

            success: function( responseObject ) {
                var relations = Ext.util.JSON.decode(responseObject.responseText).mapOrganisationUnitRelations;
                
                for (var i=0; i < relations.length; i++) {
                    var orgunitid = relations[i].organisationUnitId;
                    var featureid = relations[i].featureId;
                    
                    for (var j=0; j < mapvalues.length; j++) {
                        if (orgunitid == mapvalues[j].organisationUnitId) {
                            for (var k=0; k < features.length; k++) {
                                if (features[k].attributes['value'] == null) {
                                    features[k].attributes['value'] = 0;
                                }
                                
                                if (featureid == features[k].attributes[uniqueColumn]) {
                                    features[k].attributes['value'] = mapvalues[j].value;
                                }
                            }
                        }
                    }
                }
                
                var options = {};
                
                // hidden
                choropleth.indicator = 'value';
                choropleth.indicatorText = 'Indicator';
                options.indicator = choropleth.indicator;
                
                options.method = Ext.getCmp('method').getValue();
                options.numClasses = Ext.getCmp('numClasses').getValue();
                options.colors = choropleth.getColors();
                
                choropleth.coreComp.updateOptions(options);
                choropleth.coreComp.applyClassification();
                choropleth.classificationApplied = true;
                
                MASK.hide();
            },
            failure: function() {
                alert( 'Error while retrieving data: dataReceivedChoropleth' );
            } 
        });
    }
}

// PROPORTIONAL SYMBOL
function getPointData() {
    var indicatorId = Ext.getCmp('indicator_cb').getValue();
    var periodId = Ext.getCmp('period_cb').getValue();
    var level = parseFloat(MAPDATA.organisationUnitLevel);

    Ext.Ajax.request({
        url: path + 'getMapValues' + type,
        method: 'GET',
        params: { indicatorId: indicatorId, periodId: periodId, level: level, format: 'json' },

        success: function( responseObject ) {
            dataReceivedPoint( responseObject.responseText );
        },
        failure: function() {
            alert( 'Status', 'Error while retrieving data' );
        } 
    });
}

function dataReceivedPoint( responseText ) {
    var layers = this.myMap.getLayersByName(choroplethLayerName);
    var features = layers[0]['features'];

    var mapvalues = Ext.util.JSON.decode(responseText).mapvalues;
    
    var mlp = MAPDATA.mapLayerPath;
    var uniqueColumn = MAPDATA.uniqueColumn;
    
    Ext.Ajax.request({
        url: path + 'getAvailableMapOrganisationUnitRelations' + type,
        method: 'GET',
        params: { mapLayerPath: mlp, format: 'json' },

        success: function( responseObject ) {
            var relations = Ext.util.JSON.decode(responseObject.responseText).mapOrganisationUnitRelations;

            for (var i=0; i < relations.length; i++) {
                var orgunitid = relations[i].organisationUnitId;
                var featureid = relations[i].featureId;
                
                for (var j=0; j < mapvalues.length; j++) {
                    if (orgunitid == mapvalues[j].organisationUnitId) {
                        for (var k=0; k < features.length; k++) {
                            if (features[k].attributes['value'] == null) {
                                features[k].attributes['value'] = 0;
                            }
                            
                            if (featureid == features[k].attributes[uniqueColumn]) {
                                features[k].attributes['value'] = mapvalues[j].value;
                            }
                        }
                    }
                }
            }
            
            var minSize = Ext.getCmp('minSize').getValue();
            var maxSize = Ext.getCmp('maxSize').getValue();
            proportionalsymbol.coreComp.updateOptions({
                'indicator': this.indicator,
                'minSize': minSize,
                'maxSize': maxSize
            });
            
            proportionalsymbol.coreComp.applyClassification();
            proportionalsymbol.classificationApplied = true;
        },
        failure: function() {
            alert( 'Error while retrieving data: dataReceivedChoropleth' );
        } 
    });
}

// MAPPING
function getAssignOrganisationUnitData() {
    var mlp = MAPDATA.mapLayerPath;
    
    Ext.Ajax.request({
        url: path + 'getAvailableMapOrganisationUnitRelations' + type,
        method: 'GET',
        params: { mapLayerPath: mlp, format: 'json' },

        success: function( responseObject ) {
            dataReceivedAssignOrganisationUnit( responseObject.responseText );
        },
        failure: function() {
            alert( 'Error while retrieving data: getAssignOrganisationUnitData' );
        } 
    });
}

function dataReceivedAssignOrganisationUnit( responseText ) {
    var layers = this.myMap.getLayersByName(CHOROPLETH_LAYERNAME);
    features = layers[0]['features'];
    
    var relations = Ext.util.JSON.decode(responseText).mapOrganisationUnitRelations;
    
    var uniqueColumn = MAPDATA.uniqueColumn;   
    
    for (var i=0; i < features.length; i++) {
        var featureId = features[i].attributes[uniqueColumn];
        features[i].attributes['value'] = 0;
        
        for (var j=0; j < relations.length; j++) {
            if (relations[j].featureId == featureId) {
                features[i].attributes['value'] = 1;
            }
        }
    }
    
    var options = {};
        
    // hidden
    mapping.indicator = 'value';
    mapping.indicatorText = 'Indicator';
    options.indicator = mapping.indicator;
    
    options.method = 1;
    options.numClasses = 2;
    
    var colorA = new mapfish.ColorRgb();
    colorA.setFromHex('#FFFFFF');
    var colorB = new mapfish.ColorRgb();
    colorB.setFromHex('#72FF63');
    options.colors = [colorA, colorB]; 
    
    mapping.coreComp.updateOptions(options);
    mapping.coreComp.applyClassification();
    mapping.classificationApplied = true;
    
    MASK.hide();
}

// AUTO MAPPING

function getAutoAssignOrganisationUnitData() {
    var level = MAPDATA.organisationUnitLevel;

    Ext.Ajax.request({
        url: path + 'getOrganisationUnitsAtLevel' + type,
        method: 'POST',
        params: { level: level, format: 'json' },

        success: function( responseObject ) {
            dataReceivedAutoAssignOrganisationUnit( responseObject.responseText );
        },
        failure: function() {
            alert( 'Status', 'Error while retrieving data' );
        } 
    });
}

function dataReceivedAutoAssignOrganisationUnit( responseText ) {
    var layers = this.myMap.getLayersByName(CHOROPLETH_LAYERNAME);
    var features = layers[0]['features'];
    var organisationUnits = Ext.util.JSON.decode(responseText).organisationUnits;
    var uniqueColumn = MAPDATA.uniqueColumn;
    var nameColumn = MAPDATA.nameColumn;
    var mlp = MAPDATA.mapLayerPath;
    var count_features = 0;
    var count_orgunits = 0;
    var count_match = 0;

    for ( var j=0; j < features.length; j++ ) {
        count_features++;
        
        for ( var i=0; i < organisationUnits.length; i++ ) {
            count_orgunits++;
            
            if (features[j].attributes[uniqueColumn] == organisationUnits[i].name) {
                var organisationUnitId = organisationUnits[i].id;
                var organisationUnit = organisationUnits[i].name;
                var featureId = features[j].attributes[uniqueColumn];
                var featureName = features[j].attributes[nameColumn];
                count_match++;
                
                Ext.Ajax.request({
                    url: path + 'addOrUpdateMapOrganisationUnitRelation' + type,
                    method: 'GET',
                    params: { mapLayerPath: mlp, organisationUnitId: organisationUnitId, featureId: featureId },

                    success: function( responseObject ) {

                    },
                    failure: function() {
                        alert( 'Status', 'Error while retrieving data: dataReceivedAutoAssignOrganisationUnit' );
                    } 
                });
            }
        }
    }
    
    Ext.messageBlack.msg('Assign organisation units', + msg_highlight_start + count_match.valueOf() + msg_highlight_end + ' organisation units assigned.<br><br>Database: ' + msg_highlight_start + count_orgunits/count_features + msg_highlight_end + '<br>Shapefile: ' + msg_highlight_start + count_features + msg_highlight_end);
    
    Ext.getCmp('grid_gp').getStore().reload();
    loadMapData('assignment');
}