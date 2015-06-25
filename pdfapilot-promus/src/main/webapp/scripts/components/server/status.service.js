'use strict';

angular.module('pdfapilotpromusApp')
    .factory('StatusService', function ($http, $q) {
        return {
            threadPoolStatus: function () {
                return $http.get('api/v1/threadPoolStatus').then(function (response) {
                    return response.data;
                });
            },

            receiveThreadPoolStatus: function (callback) {
                var stompClient = null;
                var socket = new SockJS('/websocket/threadPoolStatus');
                stompClient = Stomp.over(socket);
                stompClient.connect({}, function (frame) {
                    stompClient.subscribe('/topic/threadPoolStatus', function (result) {
                        var status = JSON.parse(result.body);

                        callback(status);
                    });
                });
            },

            receiveCreatePdf: function (callback) {
                var stompClient = null;
                var socket = new SockJS('/websocket/createPdf');
                stompClient = Stomp.over(socket);
                stompClient.connect({}, function (frame) {
                    stompClient.subscribe('/topic/createPdf', function (result) {
                      var success = result.body === 'true';

                      callback(success);
                    });
                });
            }
        };
    });
