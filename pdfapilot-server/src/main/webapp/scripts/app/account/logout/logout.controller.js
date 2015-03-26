'use strict';

angular.module('pdfapilotserverApp')
    .controller('LogoutController', function (Auth) {
        Auth.logout();
    });
