'use strict';

angular.module('pdfapilotpromusApp')
    .controller('StatusController', function ($scope, Principal, StatusService, AuditsService, MonitoringService, moment) {

        StatusService.receiveThreadPoolStatus(function (status) {
            $scope.$evalAsync(function () {
                $scope.activeCount = status.activeCount;
                $scope.maximumPoolSize = status.maximumPoolSize;
            });
        });

        StatusService.receiveCreatePdf(function (success) {
            $scope.$evalAsync(function () {
                if (success) {
                    $scope.success++;
                } else {
                    $scope.failed++;
                }
            });
        });

        MonitoringService.getBasicMetrics().then(function (metrics) {
            $scope.starttime = moment().subtract(metrics.uptime, 'milliseconds');
            $scope.processors = metrics.processors;
            $scope.systemloadAverage = metrics['systemload.average'].toFixed(3);

            $scope.uptime = {
                hours: moment().subtract($scope.starttime).hours(),
                minutes: moment().subtract($scope.starttime).minutes()
            };
        }).catch(function (err) {
            console.log(err);
        });

        StatusService.threadPoolStatus().then(function (status) {
            $scope.activeCount = status.activeCount;
            $scope.maximumPoolSize = status.maximumPoolSize;
        }).catch(function (err) {
            $scope.error = err;
        });

        AuditsService.countByAuditEventType(['CREATE_PDF_SUCCESS', 'CREATE_PDFA_SUCCESS']).then(function (count) {
            $scope.success = parseInt(count);
        }).catch(function (err) {
            console.log(err);
        });

        AuditsService.countByAuditEventType(['CREATE_PDF_FAILURE', 'CREATE_PDFA_FAILURE']).then(function (count) {
            $scope.failed = parseInt(count);
        }).catch(function (err) {
            console.log(err);
        });

        Principal.identity().then(function (account) {
            $scope.account = account;
            $scope.isAuthenticated = Principal.isAuthenticated;
        });
    });
