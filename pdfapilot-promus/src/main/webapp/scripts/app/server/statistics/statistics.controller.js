'use strict';

angular.module('pdfapilotpromusApp')
    .controller('StatisticsController', function ($scope, Principal) {
        Principal.identity().then(function(account) {
            $scope.account = account;
            $scope.isAuthenticated = Principal.isAuthenticated;
        });
    });
