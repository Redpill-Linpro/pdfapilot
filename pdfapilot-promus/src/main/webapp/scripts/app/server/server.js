'use strict';

angular.module('pdfapilotpromusApp')
    .config(function ($stateProvider) {
        $stateProvider
            .state('server', {
                abstract: true,
                parent: 'site'
            });
    });
