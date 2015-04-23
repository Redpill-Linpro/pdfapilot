'use strict';

angular.module('pdfaPilotPromusApp')
    .config(function ($stateProvider) {
        $stateProvider
            .state('statistics', {
                parent: 'server',
                url: '/statistics',
                data: {
                    roles: ['ROLE_ADMIN'],
                    pageTitle: 'statistics.pageTitle'
                },
                views: {
                    'content@': {
                        templateUrl: 'scripts/app/server/statistics/statistics.html',
                        controller: 'StatisticsController'
                    }
                },
                resolve: {
                    mainTranslatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate,$translatePartialLoader) {
                        $translatePartialLoader.addPart('statistics');
                        return $translate.refresh();
                    }]
                }
            });
    });
