'use strict';

angular.module('pdfapilotpromusApp')
    .config(function ($stateProvider) {
        $stateProvider
            .state('status', {
                parent: 'server',
                url: '/status',
                data: {
                    roles: ['ROLE_ADMIN'],
                    pageTitle: 'status.pageTitle'
                },
                views: {
                    'content@': {
                        templateUrl: 'scripts/app/server/status/status.html',
                        controller: 'StatusController'
                    }
                },
                resolve: {
                    mainTranslatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate,$translatePartialLoader) {
                        $translatePartialLoader.addPart('status');
                        return $translate.refresh();
                    }]
                }
            });
    });
