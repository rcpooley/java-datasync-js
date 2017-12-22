import java.lang.reflect.Method;

public class JavaTest {

	private static class Calculator {

		public int add(int a, int b, int c) {
			return a + b + c;
		}

		public int sumAll(int... ints) {
			int sum = 0;
			for(int i = 0; i < ints.length; i++) sum += ints[i];
			return sum;
		}
	}

	public static void main(String[] args) throws Exception {
		Calculator calc = new Calculator();
		System.out.println("1 + 2 + 3 = " + calc.add(1, 2, 3));
		System.out.println("2 + 3 + 4 = " + calc.sumAll(2, 3, 4));

		System.out.println("REFLECTION:");

		Method add = Calculator.class.getMethod("add", int.class, int.class, int.class);
		Object[] addArgs = {1, 2, 3};
		int result = (int) add.invoke(calc, addArgs);
		System.out.println("1 + 2 + 3 = " + result);

		Method sumAll = Calculator.class.getMethod("sumAll", int[].class);
		int[] sumAllArgs = {2, 3, 4};
		result = (int) sumAll.invoke(calc, (Object) sumAllArgs);
		System.out.println("2 + 3 + 4 = " + result);
	}

}
