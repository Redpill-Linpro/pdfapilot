'use strict';

angular.module('pdfapilotpromusApp')
    .controller('HistoryController', function ($scope, Principal, ngTableParams, AuditsService, splash) {

        function getAuditEventType() {
            var auditEventType = [];

            if ($scope.pdfVariant === 'pdf' && $scope.success === 'true') {
                auditEventType.push('CREATE_PDF_SUCCESS');
            } else if ($scope.pdfVariant === 'pdf' && $scope.success === 'false') {
                auditEventType.push('CREATE_PDF_FAILURE');
            } else if ($scope.pdfVariant === 'pdf') {
                auditEventType.push('CREATE_PDF_SUCCESS');
                auditEventType.push('CREATE_PDF_FAILURE');
            }

            if ($scope.pdfVariant === 'pdfa' && $scope.success === 'true') {
                auditEventType.push('CREATE_PDFA_SUCCESS');
            } else if ($scope.pdfVariant === 'pdfa' && $scope.success === 'false') {
                auditEventType.push('CREATE_PDFA_FAILURE');
            } else if ($scope.pdfVariant === 'pdfa') {
                auditEventType.push('CREATE_PDFA_SUCCESS');
                auditEventType.push('CREATE_PDFA_FAILURE');
            }

            if (!$scope.pdfVariant && $scope.success === 'true') {
                auditEventType.push('CREATE_PDF_SUCCESS');
                auditEventType.push('CREATE_PDFA_SUCCESS');
            }

            if (!$scope.pdfVariant && $scope.success === 'false') {
                auditEventType.push('CREATE_PDF_FAILURE');
                auditEventType.push('CREATE_PDFA_FAILURE');
            }

            if (!$scope.pdfVariant && !$scope.success) {
                auditEventType.push('CREATE_PDF_SUCCESS');
                auditEventType.push('CREATE_PDFA_SUCCESS');
                auditEventType.push('CREATE_PDF_FAILURE');
                auditEventType.push('CREATE_PDFA_FAILURE');
            }

            return auditEventType;
        }

        $scope.tableParameters = new ngTableParams({
            page: 1,
            count: 10
        }, {
            total: 0,
            getData: function ($defer, params) {
                AuditsService.findByAuditEventType({
                    auditEventType: getAuditEventType(),
                    page: params.page(),
                    count: params.count(),
                    filename: $scope.filename,
                    username: $scope.username,
                    node: $scope.node,
                    success: $scope.success,
                    from: $scope.from ? moment($scope.from).format('YYYY-MM-DD') : null,
                    to: $scope.to ? moment($scope.to).format('YYYY-MM-DD') : null,
                    verified: $scope.verified,
                    nodeRef: $scope.nodeRef
                }).then(function (result) {
                    $defer.resolve(result.events);
                    params.total(result.total);
                    $scope.total = result.total;
                }).catch(function (err) {
                    $defer.reject(err);
                });
            }
        });

        $scope.icon = function (filename) {
            var re = /(?:\.([^.]+))?$/;
            var ext = re.exec(filename)[1];
            ext = ext ? ext.toLowerCase() : null;

            if (ext === 'doc' || ext === 'docx') {
                return 'assets/images/icons/word.png';
            } else if (ext === 'pdf') {
                return 'assets/images/icons/pdf.png';
            } else if (ext === 'ppt' || ext === 'pptx') {
                return 'assets/images/icons/powerpoint.png';
            } else if (ext === 'xls' || ext === 'xlsx') {
                return 'assets/images/icons/excel.png';
            }

            return 'assets/images/icons/unknown.png';
        };

        $scope.metadata = function (entry) {
            splash.open({
                title: 'Data',
                message: angular.toJson(entry, 3)
            });
        };

        $scope.openFrom = function ($event) {
            $event.preventDefault();
            $event.stopPropagation();

            $scope.fromOpened = true;
        };

        $scope.openTo = function ($event) {
            $event.preventDefault();
            $event.stopPropagation();

            $scope.toOpened = true;
        };

        $scope.$watchGroup(['filename', 'username', 'node', 'success', 'from', 'to', 'nodeRef', 'verified', 'pdfVariant'], function () {
            $scope.tableParameters.reload();
        });

        Principal.identity().then(function (account) {
            $scope.account = account;
            $scope.isAuthenticated = Principal.isAuthenticated;
        });
    });
