'use strict';

angular.module('pdfaPilotPromusApp')
    .config(function ($stateProvider) {
        $stateProvider
            .state('server', {
                abstract: true,
                parent: 'site'
            });
    });
