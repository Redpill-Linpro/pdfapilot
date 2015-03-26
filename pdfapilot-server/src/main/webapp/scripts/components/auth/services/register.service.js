'use strict';

angular.module('pdfapilotserverApp')
    .factory('Register', function ($resource) {
        return $resource('api/register', {}, {
        });
    });


