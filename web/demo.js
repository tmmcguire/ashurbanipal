(function() {
    "use strict";

    Ext.namespace('PG');

    function coalesce_conversion(name) { return function(v,rec) { return rec[name].join('; '); } }
    function pick_first_conversion(name) { return function(v,rec) { return rec[name][0]; } }

    function euclidianDistance(vec1, vec2) {
        var length = vec1.length;
        var d = 0.0;
        for (var i = 0; i < length; ++i) {
            d += Math.pow(vec1[i] - vec2[i], 2);
        }
        return Math.sqrt(d);
    }

    function intersect(vec1, vec2) {
        var length = vec1.length;
        var sum = 0;
        for (var i = 0; i < length; ++i) {
            // see Hacker's Delight
            var x = vec1[i] & vec2[i];
            x = x - ((x >> 1) & 0x55555555);
            x = (x & 0x33333333) + ((x >> 2) & 0x33333333);
            x = (x + (x >> 4)) & 0x0F0F0F0F;
            x = x + (x >> 8);
            x = x + (x >> 16);
            sum += x & 0x0000003F;
        }
        return 1 - (sum / 200);
    }
    PG.intersect = intersect;

    function booksByStyle(initial) {
        var books = [];
        PG.styleStore.each(function (rec) {
            var data = rec.data;
            books.push({
                etext_no: data.etext_no,
                dist:     euclidianDistance(initial, data.vector)
            });
        });
        books.sort(function(l,r) { return l.dist - r.dist; });
        return books;
    }
    PG.booksbyStyle = booksByStyle;

    function booksByTopic(initial) {
        var books = [];
        PG.topicStore.each(function(rec) {
            var data = rec.data;
            books.push({
                etext_no: data.etext_no,
                dist:     intersect(initial, data.wordset)
            });
        });
        books.sort(function(l,r) { return l.dist - r.dist; });
        return books;
    }
    PG.booksByTopic = booksByTopic;

    function booksByCombination(byStyle, byTopic) {
        var styles = { };
        var length = byStyle.length;
        for (var i = 0; i < length; ++i) {
            styles[byStyle[i].etext_no] = byStyle[i].dist;
        }
        var books = [];
        length = byTopic.length;
        for (var i = 0; i < length; ++i) {
            var etext_no = byTopic[i].etext_no;
            var distance = styles[etext_no] * Math.pow(byTopic[i].dist, 2);
            books.push({
                etext_no: etext_no,
                dist:     distance,
            });
        }
        books.sort(function(l,r) { return l.dist - r.dist; });
        return books;
    }
    PG.booksByCombination = booksByCombination;

    PG.styledataReader = new Ext.data.JsonReader({
        idProperty: 'etext_no',
        root:       'rows',
    }, [ 'etext_no', 'vector' ]);

    PG.topicdataReader = new Ext.data.JsonReader({
        idProperty: 'etext_no',
        root:       'rows',
    }, [ 'etext_no', 'wordset' ]);

    PG.metadataReader = new Ext.data.JsonReader({
        idProperty: 'etext_no',
        root:       'rows',
    }, [
        {name: 'etext_no',         convert: parseInt(pick_first_conversion('etext_no')) },
        {name: 'title',            convert: pick_first_conversion('title') },
        {name: 'author',           convert: coalesce_conversion('author') },
        {name: 'subject',          convert: coalesce_conversion('subject') },
        {name: 'copyright_status', convert: pick_first_conversion('copyright_status') },
        {name: 'note',             convert: coalesce_conversion('note') },
        {name: 'release_date',     convert: pick_first_conversion('release_date') },
        {name: 'loc_class',        convert: pick_first_conversion('loc_class') },
        {name: 'link',             convert: pick_first_conversion('link') },
        {name: 'language',         convert: pick_first_conversion('language') },
    ]);

    PG.bookTpl = new Ext.XTemplate(
        '<div><a href="{link}" style="text-decoration:none; color:#000000;" target="_blank">',
        '<p><b><i>{title},</i></b></p>',
        '<p><b>{author}</b></p>',
        '<p>{subject}</p>',
        '<tpl if="release_date"><p>Released {release_date}</p></tpl>',
        '<tpl if="loc_class"><p>Library of Congress class {loc_class}</p></tpl>',
        '<p>{language}</p>',
        '<p><em>{note}</em></p>',
        '<p>{copyright_status}</p>',
        '<p>Etext No. #{etext_no}</p>',
        '<tpl if="distance"><hr /><p>{distance}</p></tpl>',
        '</a></div>'
    );

    function cbSelectionChange(model) {
        var sel = model.getSelected();
        if (!sel) { return; }
        var selectedBook = sel.data;
        var template = PG.bookTpl;

        // Remove the distance from the selected data (if set by previous selection)
        selectedBook.distance = "";
        template.overwrite(Ext.get('book-info'), selectedBook);

        var styleRow = PG.styleStore.getById(selectedBook.etext_no);
        var topicRow = PG.topicStore.getById(selectedBook.etext_no);
        if (styleRow && topicRow) {

            var byStyle = booksByStyle( styleRow.data.vector );
            var byTopic = booksByTopic( topicRow.data.wordset );
            var byCombination = booksByCombination(byStyle, byTopic);

            var metadataStore = PG.metadataStore;

            for (var i = 1; i < 4; ++i) {
                var data = metadataStore.getById(byStyle[i].etext_no).data;
                data.distance = byStyle[i].dist.toFixed(3) + " ell";
                template.overwrite(Ext.get('style' + i), data);
            }

            for (var i = 1; i < 4; ++i) {
                var data = metadataStore.getById(byTopic[i].etext_no).data;
                data.distance = byTopic[i].dist.toFixed(3) + " bole";
                template.overwrite(Ext.get('topic' + i), data);
            }

            for (var i = 1; i < 4; ++i) {
                var data = metadataStore.getById(byCombination[i].etext_no).data;
                data.distance = byCombination[i].dist.toFixed(3) + " ell bole";
                template.overwrite(Ext.get('combined' + i), data);
            }
        }
    }

    function createGrid() {
        PG.grid = new Ext.grid.GridPanel({
            store: PG.metadataStore,
            renderTo: 'grid-example',
            columns: [
                { id: 'etext_no', header: 'EText',  dataIndex: 'etext_no', sortable: true, width: 50  },
                { id: 'title',    header: 'Title',  dataIndex: 'title',    sortable: true, width: 275 },
                { id: 'author',   header: 'Author', dataIndex: 'author',   sortable: true, width: 250 },
            ],
            stripeRows: true,
            height: 350,
            width: 600,
            selModel: new Ext.grid.RowSelectionModel({
                singleSelect: true,
                listeners: { 'selectionchange': cbSelectionChange, },
            }),
            listeners: {
                'viewready': function(grid) {
                    var row = PG.metadataStore.indexOf( PG.metadataStore.getById(773) );
                    grid.getSelectionModel().selectRow(row,false,false);
                    grid.getView().focusRow(row);
                    PG.loadMask.hide();
                }
            }
        });
    }

    PG.loaded = 0;
    function loadingCompleted() {
        PG.loaded++;
        var msg = Ext.get('loadmask').dom;
        if (PG.loaded === 1) {
            msg.innerHTML = msg.innerHTML + "<p>There's one. Go, Mr. Ferret!</p>";
        } else if (PG.loaded === 2) {
            msg.innerHTML = msg.innerHTML + "<p>There's another. (Pardon me, I have to get him some more coffee.)</p>";
        } else if (PG.loaded === 3) {
            msg.innerHTML = msg.innerHTML + "<p>That's done; fries should be up in two seconds. (Note to self: hire more ferrets.)</p>";
            createGrid();
        }
    }

    PG.start = function() {

        PG.loadMask = new Ext.LoadMask(Ext.getBody(), {
            msg: '<h3>This may take a while...</h3>' +
                '<p>The ferret is shovelling coal into the boiler as fast as he can.</p>' +
                '<p id="loadmask">(Ok, technically, we\'re loading the gigantic blobs of data so that I don\'t have to pay for server time.)</p>'
        });
        PG.loadMask.show();

        Ext.Ajax.request({
            url: "styledata.json",
            success: function(res, opt) {
                PG.styleStore = new Ext.data.Store({
                    storeId:     'styleStore',
                    reader:      PG.styledataReader,
                    data:        Ext.util.JSON.decode(res.responseText)
                });
                loadingCompleted();
            },
            failure: function(res, opt) {
                Ext.Msg.alert("Error loading style data", "Cannot load data about book styles.");
                console.log("styledata.json failure");
            },
            timeout: 60 * 1000,
        });

        Ext.Ajax.request({
            url: "topicdata.json",
            success: function(res, opt) {
                PG.topicStore = new Ext.data.Store({
                    autoLoad: false,
                    storeId:  'topicStore',
                    reader:   PG.topicdataReader,
                    data:     Ext.util.JSON.decode(res.responseText)
                });
                loadingCompleted();
            },
            failure: function(res, opt) {
                Ext.Msg.alert("Error loading topic data", "Cannot load data about book topics.");
                console.log("topiddata.json failure");
            },
            timeout: 60 * 1000,
        });

        Ext.Ajax.request({
            url: "metadata.json",
            success: function(res, opt) {
                PG.metadataStore = new Ext.data.Store({
                    storeId:     'metadataStore',
                    reader:      PG.metadataReader,
                    remoteSort:  false,
                    sortInfo:    { field:'author', direction:'ASC' },
                    data:        Ext.util.JSON.decode(res.responseText),
                });
                loadingCompleted();
            },
            failure: function(res, opt) {
                Ext.Msg.alert("Error loading metadata", "Cannot load general information about books.");
                console.log("metadata.json failure");
            },
            timeout: 60 * 1000,
        });
    }
    
}());
