// This is a manifest file that'll be compiled into application.js.
//
// Any JavaScript file within this directory can be referenced here using a relative path.
//
// You're free to add application-wide JavaScript to this file, but it's generally better 
// to create separate JavaScript files as needed.
//
//= require jquery
//= require bootstrap.min
//= require_self

if (typeof jQuery !== 'undefined') {
    console.log("Yes we have JQuery");
    (function($) {
        $('#spinner').ajaxStart(function() {
            $(this).fadeIn();
        }).ajaxStop(function() {
            $(this).fadeOut();
        });

    })(jQuery);
}

jQuery.ajax({
    url: "https://www.anbg.gov.au/25jira/s/d41d8cd98f00b204e9800998ecf8427e/en-p174ip-1988229788/6265/3/1.4.7/_/download/batch/com.atlassian.jira.collector.plugin.jira-issue-collector-plugin:issuecollector-embededjs/com.atlassian.jira.collector.plugin.jira-issue-collector-plugin:issuecollector-embededjs.js?collectorId=30647d85",
    type: "get",
    cache: true,
    dataType: "script"
});
