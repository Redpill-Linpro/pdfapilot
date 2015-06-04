'use strict';

angular.module('pdfapilotpromusApp')
    .factory('Register', function ($resource) {
        return $resource('api/register', {}, {
        });
    });


