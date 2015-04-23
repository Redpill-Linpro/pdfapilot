'use strict';

angular.module('pdfaPilotPromusApp')
    .factory('Register', function ($resource) {
        return $resource('api/register', {}, {
        });
    });


