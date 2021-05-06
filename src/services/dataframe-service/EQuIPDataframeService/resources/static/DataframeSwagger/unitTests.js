//used for testing all the servlets


var loc = window.location.pathname;
var dir = loc.substring(0, loc.lastIndexOf('/'));
var baseURL = 'http://' + window.location.host +"/EQuIPDataframeService";
var repo = window.location.search.substring(1);

if (repo.indexOf("equip") >-1) {
	var dataframeId = "6fd9a590-901d-4750-b780-0b2215615fb1";	
	var datasetId = "1373fe12-0735-4fac-a276-a872849e3927";
	var complexDataId = "76ba91b0-1b3f-443f-abc0-e2e30dc0e999";
	//TODO: need to create nodes in equip repo
//var assemblyId
//	var qcRequestId = "894bd87c-61a4-46c9-bf06-7a5f1aece492";
//	var qcWorkflowItemId = "7b8bbfc9-ae13-4184-8da7-1ea1e7f7cbde";
//	var qcChecklistItemId = "81f29852-3154-48ba-a9c7-24b386cb4b3d";
//	var qcChecklistSummaryItemId = "f00a4517-8691-4896-ab78-6456d7166ec6";
//	var commentId = "5b16bbc2-e43c-4096-8731-87bfe2c55e21";
	var metadatumId = "b3dbfd94-6172-4e02-a4b8-7feffbad2944";
}
else
	if (repo.indexOf("scratch") >-1) {
	var dataframeId = "7a7816b0-67c7-43f3-8715-a6171a6af524";	
	var datasetId = "5808fc47-3d0a-42e4-a79c-67e27a24cdae";
	//var complexDataId = "f9a9cf56-569e-4e80-96db-ffce5a435aa6";  -- not used
	var assemblyId = "cdaed589-e60d-46bc-9e12-9b3b79e6f072";
	var qcRequestId = "894bd87c-61a4-46c9-bf06-7a5f1aece492";
	var qcWorkflowItemId = "7b8bbfc9-ae13-4184-8da7-1ea1e7f7cbde";
	var qcChecklistItemId = "81f29852-3154-48ba-a9c7-24b386cb4b3d";
	var qcChecklistSummaryItemId = "f00a4517-8691-4896-ab78-6456d7166ec6";
	var commentId = "635cb06a-bbae-440e-b113-99affcd1ef0b";
	var metadatumId = "a5f80662-5086-4e9c-9665-96b1722d16bf";
	var entityId = "7a7816b0-67c7-43f3-8715-a6171a6af524";
	
	}

QUnit.module( "dataframe tests" );

QUnit.asyncTest( "Get a dataframe", function( assert ) {
	var start_time = new Date().getTime();

   	$.ajax({
   		url: baseURL + "/dataframes/" + dataframeId,
   		dataType: "json",
   		success: function (data) {
   		 var request_time = new Date().getTime() - start_time;
    	 assert.ok( true, "dataframe " + data.id + " returned in " + request_time/1000 + " seconds" );
    	 QUnit.start();
   		},
   		error: function()
   		{
   		 
   		assert.ok( false, "dataframe retrieval error" );
   		QUnit.start();                    	
   		}
   		});	
});


QUnit.asyncTest( "Post a dataframe", function( assert ) {
	var start_time = new Date().getTime();
   	$.ajax({
   		url: "data/dataframe-test.json",
   		dataType: "json",
   		success: function (test_data) {
   		   	$.ajax({
   		   		url: baseURL + "/dataframes",
   		   		data: JSON.stringify(test_data),
   		   		contentType: "application/json",
   		   		type: "POST",
   		   		success: function (data) {
   		   		 var request_time = new Date().getTime() - start_time;
   		    	 assert.ok( true, "dataframe " + data + " returned in " + request_time/1000 + " seconds" );
   		    	 QUnit.start();
   		   		},
   		   		error: function()
   		   		{
   		   		 
   		   		assert.ok( false, "Unable to post a dataframe" );
   		   		QUnit.start();                    	
   		   		}
   		   		});	
			
   		},
   		error: function()
   		{
   		 
   		assert.ok( false, "error retrieving test data" );
   		QUnit.start();                    	
   		}
   	});
});





QUnit.asyncTest( "Get a dataset", function( assert ) {
	var start_time = new Date().getTime();

   	$.ajax({
   		url: baseURL + "/dataframes/" + dataframeId + "/data",
   		dataType: "json",
   		success: function (data) {
   		 var request_time = new Date().getTime() - start_time;
    	 assert.ok( true, "dataset " + data.id + " returned in " + request_time/1000 + " seconds" );
    	 QUnit.start();
   		},
   		error: function()
   		{
   		 
   		assert.ok( false, "dataset retrieval error" );
   		QUnit.start();                    	
   		}
   		});	
});

/*
QUnit.asyncTest( "Get dataframe script", function( assert ) {
	var start_time = new Date().getTime();
	var value = "4c3df9cd-37c0-4413-b393-a45ef2e7038e";
   	$.ajax({
   		url: baseURL + "/dataframes/" + value + "/script",
   		dataType: "json",
   		success: function (data) {
   		 var request_time = new Date().getTime() - start_time;
    	 assert.ok( true, "script " + data.id + " returned in " + request_time/1000 + " seconds" );
    	 QUnit.start();
   		},
   		error: function()
   		{
   		 
   		assert.ok( false, "dataset retrieval error" );
   		QUnit.start();                    	
   		}
   		});	
});
*/
QUnit.module( "assembly tests" );
QUnit.asyncTest( "Get an assembly", function( assert ) {
	var start_time = new Date().getTime();

   	$.ajax({
   		url: baseURL + "/assemblies/" + assemblyId ,
   		dataType: "json",
   		success: function (data) {
   		 var request_time = new Date().getTime() - start_time;
    	 assert.ok( true, "assembly " + data.id + " returned in " + request_time/1000 + " seconds" );
    	 QUnit.start();
   		},
   		error: function()
   		{
   		 
   		assert.ok( false, "assembly retrieval error" );
   		QUnit.start();                    	
   		}
   		});	
});

QUnit.module( "qc request tests" );
QUnit.asyncTest( "Get a QC Request", function( assert ) {
	var start_time = new Date().getTime();
   	$.ajax({
   		url: baseURL + "/qcrequests/" + qcRequestId,
   		dataType: "json",
   		success: function (data) {
   		 var request_time = new Date().getTime() - start_time;
    	 assert.ok( true, "qc request " + data.id + " returned in " + request_time/1000 + " seconds" );
    	 QUnit.start();
   		},
   		error: function()
   		{
   		 
   		assert.ok( false, "qc request retrieval error" );
   		QUnit.start();                    	
   		}
   		});	
});
QUnit.asyncTest( "Get Workflow Items for a QC Request", function( assert ) {
	var start_time = new Date().getTime();
   	$.ajax({
   		url: baseURL + "/qcrequests/" + qcRequestId +"/qcworkflowitems",
   		dataType: "json",
   		success: function (data) {
   		 var request_time = new Date().getTime() - start_time;
    	 assert.ok( true, data.length + " workflow items returned in " + request_time/1000 + " seconds" );
    	 QUnit.start();
   		},
   		error: function()
   		{
   		 
   		assert.ok( false, "qc request retrieval error" );
   		QUnit.start();                    	
   		}
   		});	
});

QUnit.asyncTest( "Get Checklist Items for a QC Request", function( assert ) {
	var start_time = new Date().getTime();
   	$.ajax({
   		url: baseURL + "/qcrequests/" + qcRequestId +"/qcchecklistitems",
   		dataType: "json",
   		success: function (data) {
   		 var request_time = new Date().getTime() - start_time;
    	 assert.ok( true, data.length + " checklist items returned in " + request_time/1000 + " seconds" );
    	 QUnit.start();
   		},
   		error: function()
   		{
   		 
   		assert.ok( false, "qc request retrieval error" );
   		QUnit.start();                    	
   		}
   		});	
});

QUnit.asyncTest( "Get Checklist Summary Items for a QC Request", function( assert ) {
	var start_time = new Date().getTime();

   	$.ajax({
   		url: baseURL + "/qcrequests/" + qcRequestId +"/qcchecklistsummaryitems",
   		dataType: "json",
   		success: function (data) {
   		 var request_time = new Date().getTime() - start_time;
    	 assert.ok( true, data.length + " checklist summary items returned in " + request_time/1000 + " seconds" );
    	 QUnit.start();
   		},
   		error: function()
   		{
   		 
   		assert.ok( false, "qc request retrieval error" );
   		QUnit.start();                    	
   		}
   		});	
});

QUnit.module( "entity tests" );


QUnit.asyncTest( "Get Comments for an object", function( assert ) {
	var start_time = new Date().getTime();

   	$.ajax({
   		url: baseURL + "/entities/" + entityId +"/comments",
   		dataType: "json",
   		success: function (data) {
   		 var request_time = new Date().getTime() - start_time;
    	 assert.ok( true, data.length + " comments returned in " + request_time/1000 + " seconds" );
    	 QUnit.start();
   		},
   		error: function()
   		{
   		 
   		assert.ok( false, "comment retrieval error" );
   		QUnit.start();                    	
   		}
   		});	
});

QUnit.asyncTest( "Get Metadata for an object", function( assert ) {
	var start_time = new Date().getTime();

   	$.ajax({
   		url: baseURL + "/entities/" + entityId +"/metadata",
   		dataType: "json",
   		success: function (data) {
   		 var request_time = new Date().getTime() - start_time;
    	 assert.ok( true, data.length + " metadata returned in " + request_time/1000 + " seconds" );
    	 QUnit.start();
   		},
   		error: function()
   		{
   		 
   		assert.ok( false, "metadata retrieval error" );
   		QUnit.start();                    	
   		}
   		});	
});
