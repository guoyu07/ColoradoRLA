import * as React from 'react';

import RoundContainer from './RoundContainer';

import fetchReport from 'corla/action/dos/fetchReport';


interface RiskLimitInfoProps {
    riskLimit: number;
}

const RiskLimitInfo = ({ riskLimit }: RiskLimitInfoProps) => {
    return (
        <div className='pt-card'>
            <strong>Target Risk Limit: </strong> { riskLimit * 100 } %
        </div>
    );
};

interface SeedInfoProps {
    seed: string;
}

const SeedInfo = ({ seed }: SeedInfoProps) => {
    return (
        <div className='pt-card'>
            <strong>Seed: </strong> { seed }
        </div>
    );
};

interface DefinitionProps {
    dosState: DOS.AppState;
}

const Definition = ({ dosState }: DefinitionProps) => {
    return (
        <div>
            <RiskLimitInfo riskLimit={ dosState.riskLimit! } />
            <SeedInfo seed={ dosState.seed } />
        </div>
    );
};

const NotDefined = () => {
    return (
        <div><em>The audit has not yet been defined.</em></div>
    );
};

interface MainProps {
    auditDefined: boolean;
    canRenderReport: boolean;
    dosState: DOS.AppState;
}

const Main = (props: MainProps) => {
    const { auditDefined, canRenderReport, dosState } = props;

    const auditDefinition = auditDefined
                          ? <Definition dosState={ dosState } />
                          : <NotDefined />;

    if (!dosState.asm) {
        return null;
    }

    if (dosState.asm.currentState === 'DOS_AUDIT_COMPLETE') {
        return (
            <div className='sos-notifications pt-card'>
                { auditDefinition }
                <div className='pt-card'>
                    Congratulations! The audit is complete.
                </div>
                <div className='pt-card'>
                    <div>Click to download final audit report.</div>
                    <button
                        className='pt-button'
                        onClick={ fetchReport }>
                        Download
                    </button>
                </div>
            </div>
        );
    }

    return (
        <div className='sos-notifications pt-card'>
            { auditDefinition }
            <RoundContainer />
            <div className='pt-card'>
                <div>Click to download intermediate audit report.</div>
                <button
                    className='pt-button'
                    disabled={ !canRenderReport }
                    onClick={ fetchReport }>
                    Download
                </button>
            </div>
        </div>
    );
};


export default Main;
