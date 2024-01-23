"""
stats_functions.py

Description:
    Provides statistical functions to compute various measures on the yield data. 
    Aids in quantifying the alignment between model predictions and historical data.
    Also can be used to compare different historical yield sources (ie SPAM and FAOSTAT).

Author:
    Morgan Rivers
"""

import numpy as np
import statsmodels.api as sm
from statsmodels.tools.eval_measures import rmse as statmodel_rmse
from scipy.stats import linregress


class StatsFunctions:
    @staticmethod
    def RMSE(expected, observed):
        log_ratios = np.log(observed / expected)
        squared_diffs = log_ratios**2
        mean_of_differences = np.mean(squared_diffs)
        result = np.sqrt(mean_of_differences)
        return result / np.log(2)

    @staticmethod
    def weighted_RMSE(expected, observed, weights):
        log_ratios = np.log(observed / expected)
        weighted_squared_diffs = log_ratios**2
        avg_squared_diffs = np.average(weighted_squared_diffs, weights=weights)
        result = np.sqrt(avg_squared_diffs)
        return result / np.log(2)

    @staticmethod
    def d_statistic(expected, observed):
        O = list(expected)
        P = list(observed)

        numerator = 0
        denominator = 0

        N = len(P)
        if N != len(O):
            raise ValueError("P and O must be the same length")

        mean_O = sum(O) / N
        for i in range(N):
            P_prime = P[i] - mean_O
            O_prime = O[i] - mean_O

            numerator += (P[i] - O[i]) ** 2
            denominator += (abs(P_prime) + abs(O_prime)) ** 2

        if denominator == 0:
            raise ValueError("Denominator is zero")

        result = 1 - (numerator / denominator)

        return result

    @staticmethod
    def fraction_rmse(expected, observed):
        errors = expected - observed
        normalized_errors = errors / observed

        # RMSE of the normalized errors
        rmse = np.sqrt(np.mean(normalized_errors**2))

        return rmse

    @staticmethod
    def rmse_test(expected, observed):
        errors = expected - observed

        # RMSE of the normalized errors
        rmse = np.sqrt(np.mean(errors**2))

        return rmse

    @staticmethod
    def get_stats(world, observed_col, expected_col, weights):
        # Weighted least squares regression
        X = sm.add_constant(world[expected_col])
        y = world[observed_col]
        # breakpoint()
        # print("weights")
        # print(weights)
        model = sm.WLS(y, X, weights=weights).fit()
        # Calculate r-squared
        slope, intercept, r_value, p_value, std_err = linregress(
            world[expected_col], world[observed_col]
        )

        # Assumingexpected_colis the expected values
        expected_values = world[expected_col]
        # Assumingobserved_colis the observed results
        observed_results = world[observed_col]

        rmse = StatsFunctions.RMSE(expected_values, expected_values)

        # weighted_rmse = weighted_RMSE(expected_values, observed_results, weights)
        d_stat = StatsFunctions.d_statistic(expected_values, observed_results)
        assert statmodel_rmse(expected_values, expected_values) == 0
        linear_rmse = statmodel_rmse(expected_values, observed_results)
        # print("rmse vs test")
        # print(round(linear_rmse, 4))
        # print(round(rmse_test(expected_values, observed_results), 4))
        # assert round(rmse_test(expected_values, observed_results), 4) == round(
        #     linear_rmse, 4
        # ), "ERROR: looks like your data may not exist, OR, my algorithm is wrong :)\
        # Make sure you've processed all the crops you're analyzing..."
        relative_rmse = linear_rmse / np.mean(expected_values)

        return r_value**2, model.rsquared, rmse, relative_rmse, d_stat, linear_rmse

    @staticmethod
    def get_weights(world, observed_col, expected_col):
        print("world4")
        print(world)
        print(world.columns)
        weights = world[expected_col + "_production"] / np.average(
            world[expected_col + "_production"]
        )

        # Show rows where 'expected_col_production' has NaN values
        # rows_with_nan = world[world[expected_col + "_production"].isna()]

        # print("\nRows where 'expected_col_production' is NaN:")
        # print(rows_with_nan)

        return weights
