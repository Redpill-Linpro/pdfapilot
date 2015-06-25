'use strict';

angular.module('pdfapilotpromusApp')
    .config(function ($stateProvider) {
        $stateProvider
            .state('history', {
                parent: 'server',
                url: '/history',
                data: {
                    roles: ['ROLE_ADMIN'],
                    pageTitle: 'history.pageTitle'
                },
                views: {
                    'content@': {
                        templateUrl: 'scripts/app/server/history/history.html',
                        controller: 'HistoryController'
                    }
                },
                resolve: {
                    mainTranslatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate,$translatePartialLoader) {
                        $translatePartialLoader.addPart('history');
                        return $translate.refresh();
                    }]
                }
            });
    });
