import * as _ from 'lodash';

import countyDashboardRefreshOk from './county/dashboardRefreshOk';
import fetchAuditBoardASMStateOk from './county/fetchAuditBoardASMStateOk';
import countyFetchContestsOk from './county/fetchContestsOk';
import fetchCountyASMStateOk from './county/fetchCountyASMStateOk';
import countyFetchCvrOk from './county/fetchCvrOk';
import fetchCvrsToAuditOk from './county/fetchCvrsToAuditOk';
import countyLoginOk from './county/loginOk';
import updateAcvrForm from './county/updateAcvrForm';
import uploadAcvrOk from './county/uploadAcvrOk';
import uploadBallotManifestOk from './county/uploadBallotManifestOk';
import uploadCvrExportOk from './county/uploadCvrExportOk';
import uploadingBallotManifest from './county/uploadingBallotManifest';
import uploadingCvrExport from './county/uploadingCvrExport';

import dosContestFetchOk from './dos/contestFetchOk';
import dosDashboardRefreshOk from './dos/dashboardRefreshOk';
import fetchDOSASMStateOk from './dos/fetchDOSASMStateOk';
import dosLoginOk from './dos/loginOk';
import selectContestsForAuditOk from './dos/selectContestsForAuditOk';
import uploadRandomSeedOk from './dos/uploadRandomSeedOk';

import login1FOk from './login1FOk';


export const defaultCountyState = (): County.AppState => ({
    acvrs: {},
    asm: {
        auditBoard: {},
        county: {},
    },
    auditBoard: [],
    contests: [],
    type: 'County',
});

export const defaultDOSState = (): DOS.AppState => ({
    asm: { currentState: 'DOS_INITIAL_STATE' },
    auditedContests: {},
    countyStatus: {},
    type: 'DOS',
});

const defaultLoginState: LoginAppState = { type: 'Login' };

const defaultState = defaultLoginState;


export default function root(state: AppState = defaultState, action: Action.App) {
    switch (action.type) {

    case 'COUNTY_DASHBOARD_REFRESH_OK': {
        return countyDashboardRefreshOk(state as County.AppState, action);
    }

    case 'COUNTY_FETCH_CONTESTS_OK': {
        return countyFetchContestsOk(state as County.AppState, action);
    }

    case 'COUNTY_FETCH_CVR_OK': {
        return countyFetchCvrOk(state as County.AppState, action);
    }

    case 'COUNTY_LOGIN_OK': {
        return countyLoginOk(state as LoginAppState);
    }

    case 'DOS_DASHBOARD_REFRESH_OK': {
        return dosDashboardRefreshOk(state as DOS.AppState, action);
    }

    case 'DOS_FETCH_CONTESTS_OK': {
        return dosContestFetchOk(state as DOS.AppState, action);
    }

    case 'DOS_LOGIN_OK': {
        return dosLoginOk(state as LoginAppState);
    }

    case 'FETCH_AUDIT_BOARD_ASM_STATE_OK': {
        return fetchAuditBoardASMStateOk(state as County.AppState, action);
    }

    case 'FETCH_COUNTY_ASM_STATE_OK': {
        return fetchCountyASMStateOk(state as County.AppState, action);
    }

    case 'FETCH_CVRS_TO_AUDIT_OK': {
        return fetchCvrsToAuditOk(state as County.AppState, action);
    }

    case 'FETCH_DOS_ASM_STATE_OK': {
        return fetchDOSASMStateOk(state as DOS.AppState, action);
    }

    case 'LOGIN_1F_OK': {
        return login1FOk(state, action);
    }

    case 'SELECT_CONTESTS_FOR_AUDIT_OK': {
        return selectContestsForAuditOk(state as DOS.AppState, action);
    }

    case 'UPDATE_ACVR_FORM': {
        return updateAcvrForm(state as County.AppState, action);
    }

    case 'UPLOAD_BALLOT_MANIFEST_OK': {
        return uploadBallotManifestOk(state as County.AppState, action);
    }

    case 'UPLOAD_ACVR_OK': {
        return uploadAcvrOk(state as County.AppState, action);
    }

    case 'UPLOAD_CVR_EXPORT_OK': {
        return uploadCvrExportOk(state as County.AppState, action);
    }

    case 'UPLOAD_RANDOM_SEED_OK': {
        return uploadRandomSeedOk(state as DOS.AppState, action);
    }

    case 'UPLOADING_BALLOT_MANIFEST': {
        return uploadingBallotManifest(state as County.AppState, action);
    }

    case 'UPLOADING_CVR_EXPORT': {
        return uploadingCvrExport(state as County.AppState, action);
    }

    default:
        return state;
    }
}
