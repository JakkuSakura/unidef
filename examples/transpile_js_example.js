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
};