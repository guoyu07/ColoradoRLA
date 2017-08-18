import * as React from 'react';
import { connect } from 'react-redux';
import { bindActionCreators } from 'redux';

import BallotAuditStage from './BallotAuditStage';

import ballotNotFound from '../../../../action/ballotNotFound';
import countyDashboardRefresh from '../../../../action/countyDashboardRefresh';
import fetchCvrById from '../../../../action/fetchCvrById';


class BallotAuditStageContainer extends React.Component<any, any> {
    public render() {
        return <BallotAuditStage { ...this.props } />;
    }
}

const mapStateToProps = ({ county }: any) => {
    const { currentBallot } = county;

    return { county, currentBallot };
};

const mapDispatchToProps = (dispatch: any) => bindActionCreators({
    ballotNotFound,
    countyDashboardRefresh,
    fetchCvrById,
    updateBallotMarks: (data: any) => ({
        data,
        type: 'UPDATE_ACVR_FORM',
    }),
}, dispatch);

export default connect(
    mapStateToProps,
    mapDispatchToProps,
)(BallotAuditStageContainer);
