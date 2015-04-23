'use strict';

angular.module('pdfaPilotPromusApp')
    .controller('StatisticsController', function ($scope, Principal) {
        Principal.identity().then(function(account) {
            $scope.account = account;
            $scope.isAuthenticated = Principal.isAuthenticated;
        });
    });
