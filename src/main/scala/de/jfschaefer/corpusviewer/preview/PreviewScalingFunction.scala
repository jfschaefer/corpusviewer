package de.jfschaefer.corpusviewer.preview

/** A function that can be used in the preview group for the scaling of the preview images */
abstract class AbstractPreviewScalingFunction {
  /** The actual scaling function (function(x) is the scaling factor for a Displayable at position x)
    * The range [-1, 1] corresponds to the screen height
    * Other code probably assumes that the function is symmetric wrt x = 0
    * Obviously, the value range has to be non-negative (for x in [-1, 1])
    * The integral over [-1, 1] should be 2, otherwise the margins between the previews won't be correct
    *
    * @param x x
    * @return f(x)
    */
  def function(x: Double): Double

  /** The integral over the function
    *
    * @param x x
    * @return integral from a to x over function(x), where a is some constant
    */
  def integral(x: Double): Double

  /** The normalized integral
    *
    * transformation of the integral such that normalizedIntegral(-1) = 0 and normalizedIntegral(1) = 1
    *
    * @param x x
    * @return the normalized integral of x
    */
  def normalizedIntegral(x: Double): Double = {
    val at_x0 = integral(-1d)
    val total = integral(1d) - at_x0
    (integral(x) - at_x0) / total
  }

  /** inverse of the normalizedIntegral
    *
    * The inverse exists over [-1, 1], as function(x) is non-negative for all x in [-1, 1]
    *
    * @param x x
    * @return y such that normalizedIntegral(y) - x = 0
    */
  def normalizedIntegralInverse(x: Double): Double = {
    // result should be in the range [-2, 2] (actually [-1, 1], but let's accept one more iteration to be safe)
    secantMethod(-2, 2, normalizedIntegral(_) - x, 40)
  }

  assert(secantMethod(3.0, 4.0, math.sin, 25) <= 3.142)
  assert(secantMethod(3.0, 4.0, math.sin, 25) >= 3.141)

  /** A simple implementation of the secant method
    *
    * Returns a value x, a0 < x < b0, such that approximately f(x) = 0
    * Preconditions: a0 < b0, f smooth, f(a0) * f(b0) < 0
    *
    * @param a0 the interval start
    * @param b0 the interval end
    * @param f the function
    * @param maxIter the maximal number of iterations
    * @return x
    */
  def secantMethod(a0: Double, b0: Double, f: Double => Double, maxIter: Int) : Double = {
    val epsilon = 1e-12
    var a = a0
    var b = b0
    var iter = 0
    assert(math.signum(f(a)) != math.signum(f(b)))
    while (iter < maxIter) {
      iter = iter + 1
      val c = 0.5 * (a + b)
      if (math.abs(f(c)) < epsilon) return c
      if (math.signum(f(a)) == math.signum(f(c))) a = c else b = c
    }
    0.5 * (a + b)
  }
}

/** Constant scaling */
class ConstantScalingFunction extends AbstractPreviewScalingFunction {
  override def function(x: Double): Double = 1d
  override def integral(x: Double): Double = x
  //override def reciprocalIntegral(x: Double): Double = x
}

/** Polynomial scaling */
class PolynomialScalingFunction extends AbstractPreviewScalingFunction {
  override def function(x: Double): Double = (2/1.3) * (if (x < -1 || x > 1) 0.25 else 1 - (x*x) * (1.5 - 0.75 * x*x))
  override def integral(x: Double): Double = (2/1.3) * (
    if (x < -1)
      -0.65 + 0.25 * (x+1)
    else if (x > 1)
      0.65 + 0.25 * (x - 1)
    else
      0.25 * x * (0.6 * x*x*x*x - 2 * x*x + 4)
    )

  /*
  def atanh(z: Complex): Complex = {
    val i = new Complex(0d, 1d)
    i.reciprocal().multiply((i.multiply(z)).atan())
  }

  //integral over the hard interval of reciprocal function
  def helper(x: Double): Double = {
    //according to wolfram alpha: integral 1/(1-x x (1.5-0.75 x x)) dx =
    // (1.03795-0.278119 i) tan^(-1)((0.240858+0.898895 i) x)+(0.278119-1.03795 i) tanh^(-1)((0.898895+0.240858 i) x)+constant

    val c1 = new Complex(1.03795, -0.278119)
    val c2 = new Complex(0.278119, -1.03795)
    val a1 = new Complex(0.240858*x, 0.898895*x).atan()
    val a2 = atanh(new Complex(0.898895*x, 0.240858*x))
    a1.add(a2).getReal()
  }

  override def reciprocalIntegral(x: Double): Double = {
    if (x < -1) {
      helper(-1) + 4 * (x + 1)
    } else if (x > 1) {
      helper(1) + 4 * (x - 1)
    } else {
      helper(x)
    }
  }
  */
}