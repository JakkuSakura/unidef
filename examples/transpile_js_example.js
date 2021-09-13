'use strict';

//  ---------------------------------------------------------------------------

const Exchange = require('./base/Exchange');
const {
    ExchangeError,
    ArgumentsRequired
} = require('./base/errors');
const {TRUNCATE} = require('./base/functions/number');
//  ---------------------------------------------------------------------------

module.exports = class binance extends Exchange {
    calculateRateLimiterCost(api, method, path, params, config = {}, context = {}) {
        if (('noSymbol' in config) && !('symbol' in params)) {
            return config['noSymbol'];
        } else if (('noPoolId' in config) && !('poolId' in params)) {
            return config['noPoolId'];
        } else if (('byLimit' in config) && ('limit' in params)) {
            const limit = params['limit'];
            const byLimit = config['byLimit'];
            for (let i = 0; i < byLimit.length; i++) {
                const entry = byLimit[i];
                if (limit <= entry[0]) {
                    return entry[1];
                }
            }
        }
        return this.safeInteger(config, 'cost', 1);
    }

    describe() {
        return this.deepExtend(super.describe(), {
            // new metainfo interface
            'timeframes': {
                '1m': '1m',
                '3m': '3m'
            },
            'depth': 1,
            'fees': {
                'trading': {
                    'taker': this.parseNumber('0.001'),
                    'maker': this.parseNumber('0.001'),
                },
            },
        });
    }

    costToPrecision(symbol, cost) {
        return this.decimalToPrecision(cost, TRUNCATE, this.markets[symbol]['precision']['quote'], this.precisionMode, this.paddingMode);
    }

    async costToPrecisionAsync(symbol, cost) {
        return this.decimalToPrecision(cost, TRUNCATE, this.markets[symbol]['precision']['quote'], this.precisionMode, this.paddingMode);
    }

    test_mutable(b) {
        let a = 0;
        if (a & 1 === 0) {
            a = 10;
            b = 6;
        }
        return a * b;
    }

};