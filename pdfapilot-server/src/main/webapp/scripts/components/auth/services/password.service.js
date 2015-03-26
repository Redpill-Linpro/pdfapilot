'use strict';

angular.module('pdfapilotserverApp')
    .factory('Password', function ($resource) {
        return $resource('api/account/change_password', {}, {
        });
    });
