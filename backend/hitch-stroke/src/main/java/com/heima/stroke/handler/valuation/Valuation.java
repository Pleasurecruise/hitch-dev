package com.heima.stroke.handler.valuation;

//TODO:任务3.3-使用装饰者模式，完成路径计费
//3.3 完成计费功能，给orderPo设置金额
//计费规则：3公里以内起步价13元；3公里以上2.3元/公里；燃油附加费1次收取1元
//建议：使用装饰着模式来完成
public interface Valuation {
    //基础计费
    float calculation(float km);
    public class BasicValuation implements Valuation {
        private Valuation valuation;
        public BasicValuation(Valuation valuation) {
            this.valuation = valuation;
        }
        @Override
        public float calculation(float km) {
            return 13f;
        }
    }
    //燃料附加费
    public class FuelValuation implements Valuation {
        private Valuation valuation;
        public FuelValuation(Valuation valuation) {
            this.valuation = valuation;
        }
        @Override
        public float calculation(float km) {
            float cost = valuation.calculation(km);
            //每3公里加1元
            return cost + km/3;
        }
    }
    //额外计费
    public class ExtraPriceValuation implements Valuation {
        private Valuation valuation;
        public ExtraPriceValuation(Valuation valuation) {
            this.valuation = valuation;
        }
        @Override
        public float calculation(float km) {
            float cost = valuation.calculation(km);
            //3公里以上2.3元/公里
            if (km > 3) {
                return cost + (km - 3) * 2.3f;
            }
            return cost;
        }
    }
    //总计费
    public class TotalValuation implements Valuation {
        private Valuation basicValuation;
        private Valuation fuelValuation;
        private Valuation extraPriceValuation;
        public TotalValuation(Valuation basicValuation, Valuation fuelValuation, Valuation extraPriceValuation) {
            this.basicValuation = basicValuation;
            this.fuelValuation = fuelValuation;
            this.extraPriceValuation = extraPriceValuation;
        }
        @Override
        public float calculation(float km) {
            float basicCost = basicValuation.calculation(km);
            float fuelCost = fuelValuation.calculation(km);
            float extraCost = extraPriceValuation.calculation(km);
            return basicCost + fuelCost + extraCost;
        }
    }
}
