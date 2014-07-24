package org.javamoney.moneta;

import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.money.MonetaryContext;

import org.javamoney.moneta.Money;
import org.javamoney.moneta.spi.MonetaryConfig;

/**
 * Evaluates the default {@link MonetaryContext} to be used for {@link Money}.
 * The default {@link MonetaryContext} can be configured by adding a file
 * {@code /javamoney.properties} from the classpath with the following content:
 * <p/>
 * 
 * <pre>
 * # Default MathContext for Money
 * #-------------------------------
 * # Custom MathContext, overrides entries from org.javamoney.moneta.Money.mathContext
 * # RoundingMode hereby is optional (default = HALF_EVEN)
 * org.javamoney.moneta.Money.defaults.precision=256
 * org.javamoney.moneta.Money.defaults.roundingMode=HALF_EVEN
 * </pre>
 * <p/>
 * Hereby the roundingMode constants are the same as defined on
 * {@link RoundingMode}.
 *
 * @return default MonetaryContext, never {@code null}.
 */
class DefaultMonetaryContextFactory {

	public MonetaryContext getContext() {
		try {
			Map<String, String> config = MonetaryConfig.getConfig();
			String value = config.get("org.javamoney.moneta.Money.defaults.precision");
			if (Objects.nonNull(value)) {
				return createMonetaryContextNonNullConfig(config, Integer.parseInt(value));
			} else {
				return createContextWithConfig(config);
			}
		} catch (Exception e) {
			Logger.getLogger(DefaultMonetaryContextFactory.class.getName())
					.log(Level.SEVERE, "Error evaluating default NumericContext, using default (NumericContext.NUM64).", e);
			return new MonetaryContext.Builder(Money.class).set(MathContext.DECIMAL64).build();
		}
	}

	private MonetaryContext createContextWithConfig(Map<String, String> config) {

		MonetaryContext.Builder builder = new MonetaryContext.Builder(Money.class);
		String value = config.get("org.javamoney.moneta.Money.defaults.mathContext");
		if (Objects.nonNull(value)) {
			switch (value.toUpperCase(Locale.ENGLISH)) {
			case "DECIMAL32":
				Logger.getLogger(Money.class.getName()).info(
						"Using MathContext.DECIMAL32");
				builder.set(MathContext.DECIMAL32);
				break;
			case "DECIMAL64":
				Logger.getLogger(Money.class.getName()).info(
						"Using MathContext.DECIMAL64");
				builder.set(MathContext.DECIMAL64);
				break;
			case "DECIMAL128":
				Logger.getLogger(Money.class.getName()).info(
						"Using MathContext.DECIMAL128");
				builder.set(MathContext.DECIMAL128);
				break;
			case "UNLIMITED":
				Logger.getLogger(Money.class.getName()).info(
						"Using MathContext.UNLIMITED");
				builder.set(MathContext.UNLIMITED);
				break;
			}
		} else {
			Logger.getLogger(Money.class.getName()).info(
					"Using default MathContext.DECIMAL64");
			builder.set(MathContext.DECIMAL64);
		}
		return builder.build();
	}

	private MonetaryContext createMonetaryContextNonNullConfig(Map<String, String> config, int prec) {
		String value = config.get("org.javamoney.moneta.Money.defaults.roundingMode");
		RoundingMode rm = Objects.nonNull(value) ? RoundingMode.valueOf(value
				.toUpperCase(Locale.ENGLISH)) : RoundingMode.HALF_UP;
		MonetaryContext mc = new MonetaryContext.Builder(Money.class)
				.setPrecision(prec).set(rm).set(Money.class).build();
		Logger.getLogger(DefaultMonetaryContextFactory.class.getName()).info("Using custom MathContext: precision=" + prec
						+ ", roundingMode=" + rm);
		return mc;
	}
}
