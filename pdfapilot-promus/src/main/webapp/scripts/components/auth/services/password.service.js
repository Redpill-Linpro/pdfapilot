'use strict';

angular.module('pdfaPilotPromusApp')
    .factory('Password', function ($resource) {
        return $resource('api/account/change_password', {}, {
        });
    });
