import * as React from 'react';
import { connect } from 'react-redux';

import DOSDashboardPage from './Page';

import withPoll from 'corla/component/withPoll';


class DOSDashboardContainer extends React.Component<any, any> {
    public render() {
        if (!this.props.sos) {
            return <div />;
        }

        return <DOSDashboardPage { ...this.props } />;
    }
}

const select = (state: any) => {
    const { sos } = state;

    if (!sos) { return {}; }

    return {
        contests: sos.contests,
        countyStatus: sos.countyStatus,
        seed: sos.seed,
        sos,
    };
};


export default withPoll(
    DOSDashboardContainer,
    'DOS_DASHBOARD_POLL_START',
    'DOS_DASHBOARD_POLL_STOP',
    select,
);
