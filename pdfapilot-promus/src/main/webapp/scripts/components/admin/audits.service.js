'use strict';

angular.module('pdfaPilotPromusApp')
    .factory('AuditsService', function ($http) {
        return {
            findAll: function () {
                return $http.get('api/audits/all').then(function (response) {
                    return response.data;
                });
            },
            findByDates: function (fromDate, toDate) {
                var formatDate =  function (dateToFormat) {
                    if (dateToFormat !== undefined && !angular.isString(dateToFormat)) {
                        return dateToFormat.getYear() + '-' + dateToFormat.getMonth() + '-' + dateToFormat.getDay();
                    }
                    return dateToFormat;
                };

                return $http.get('api/audits/byDates', {params: {fromDate: formatDate(fromDate), toDate: formatDate(toDate)}}).then(function (response) {
                    return response.data;
                });
            },
            countByAuditEventType: function(auditEventType) {
                return $http.get('api/audits/countByAuditEventType', {
                    params: {
                      auditEventType: auditEventType
                    }
                }).then(function (response) {
                    return response.data;
                });
            },
            findByAuditEventType: function(query) {
                return $http.get('api/audits/findByAuditEventType', {
                    params: query
                }).then(function (response) {
                    return response.data;
                });
            }
        };
    });
