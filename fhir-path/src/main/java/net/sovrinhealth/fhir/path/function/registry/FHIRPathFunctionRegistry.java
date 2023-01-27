/*
 * (C) Copyright IBM Corp. 2019, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.path.function.registry;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.sovrinhealth.fhir.path.function.AllFalseFunction;
import net.sovrinhealth.fhir.path.function.AllTrueFunction;
import net.sovrinhealth.fhir.path.function.AnyFalseFunction;
import net.sovrinhealth.fhir.path.function.AnyTrueFunction;
import net.sovrinhealth.fhir.path.function.BetweenFunction;
import net.sovrinhealth.fhir.path.function.CheckModifiersFunction;
import net.sovrinhealth.fhir.path.function.ChildrenFunction;
import net.sovrinhealth.fhir.path.function.CombineFunction;
import net.sovrinhealth.fhir.path.function.ConformsToFunction;
import net.sovrinhealth.fhir.path.function.ContainsFunction;
import net.sovrinhealth.fhir.path.function.ConvertsToBooleanFunction;
import net.sovrinhealth.fhir.path.function.ConvertsToDateFunction;
import net.sovrinhealth.fhir.path.function.ConvertsToDateTimeFunction;
import net.sovrinhealth.fhir.path.function.ConvertsToDecimalFunction;
import net.sovrinhealth.fhir.path.function.ConvertsToIntegerFunction;
import net.sovrinhealth.fhir.path.function.ConvertsToQuantityFunction;
import net.sovrinhealth.fhir.path.function.ConvertsToStringFunction;
import net.sovrinhealth.fhir.path.function.ConvertsToTimeFunction;
import net.sovrinhealth.fhir.path.function.CountFunction;
import net.sovrinhealth.fhir.path.function.DescendantsFunction;
import net.sovrinhealth.fhir.path.function.DistinctFunction;
import net.sovrinhealth.fhir.path.function.EmptyFunction;
import net.sovrinhealth.fhir.path.function.EndsWithFunction;
import net.sovrinhealth.fhir.path.function.ExcludeFunction;
import net.sovrinhealth.fhir.path.function.ExpandFunction;
import net.sovrinhealth.fhir.path.function.ExtensionFunction;
import net.sovrinhealth.fhir.path.function.FHIRPathFunction;
import net.sovrinhealth.fhir.path.function.FirstFunction;
import net.sovrinhealth.fhir.path.function.GetValueFunction;
import net.sovrinhealth.fhir.path.function.HasValueFunction;
import net.sovrinhealth.fhir.path.function.HtmlChecksFunction;
import net.sovrinhealth.fhir.path.function.IndexOfFunction;
import net.sovrinhealth.fhir.path.function.IntersectFunction;
import net.sovrinhealth.fhir.path.function.IsDistinctFunction;
import net.sovrinhealth.fhir.path.function.ItemFunction;
import net.sovrinhealth.fhir.path.function.LastFunction;
import net.sovrinhealth.fhir.path.function.LengthFunction;
import net.sovrinhealth.fhir.path.function.LookupFunction;
import net.sovrinhealth.fhir.path.function.LowerFunction;
import net.sovrinhealth.fhir.path.function.MatchesFunction;
import net.sovrinhealth.fhir.path.function.MemberOfFunction;
import net.sovrinhealth.fhir.path.function.NotFunction;
import net.sovrinhealth.fhir.path.function.NowFunction;
import net.sovrinhealth.fhir.path.function.ReplaceFunction;
import net.sovrinhealth.fhir.path.function.ReplaceMatchesFunction;
import net.sovrinhealth.fhir.path.function.ResolveFunction;
import net.sovrinhealth.fhir.path.function.SingleFunction;
import net.sovrinhealth.fhir.path.function.SkipFunction;
import net.sovrinhealth.fhir.path.function.SliceFunction;
import net.sovrinhealth.fhir.path.function.StartsWithFunction;
import net.sovrinhealth.fhir.path.function.SubsetOfFunction;
import net.sovrinhealth.fhir.path.function.SubstringFunction;
import net.sovrinhealth.fhir.path.function.SubsumedByFunction;
import net.sovrinhealth.fhir.path.function.SubsumesFunction;
import net.sovrinhealth.fhir.path.function.SupersetOfFunction;
import net.sovrinhealth.fhir.path.function.TailFunction;
import net.sovrinhealth.fhir.path.function.TakeFunction;
import net.sovrinhealth.fhir.path.function.TimeOfDayFunction;
import net.sovrinhealth.fhir.path.function.ToBooleanFunction;
import net.sovrinhealth.fhir.path.function.ToCharsFunction;
import net.sovrinhealth.fhir.path.function.ToDateFunction;
import net.sovrinhealth.fhir.path.function.ToDateTimeFunction;
import net.sovrinhealth.fhir.path.function.ToDecimalFunction;
import net.sovrinhealth.fhir.path.function.ToIntegerFunction;
import net.sovrinhealth.fhir.path.function.ToQuantityFunction;
import net.sovrinhealth.fhir.path.function.ToStringFunction;
import net.sovrinhealth.fhir.path.function.ToTimeFunction;
import net.sovrinhealth.fhir.path.function.TodayFunction;
import net.sovrinhealth.fhir.path.function.TranslateFunction;
import net.sovrinhealth.fhir.path.function.TypeFunction;
import net.sovrinhealth.fhir.path.function.UnionFunction;
import net.sovrinhealth.fhir.path.function.UpperFunction;
import net.sovrinhealth.fhir.path.function.ValidateCSFunction;
import net.sovrinhealth.fhir.path.function.ValidateVSFunction;

public final class FHIRPathFunctionRegistry {
    private static final FHIRPathFunctionRegistry INSTANCE = new FHIRPathFunctionRegistry();
    private Map<String, FHIRPathFunction> functionMap = new ConcurrentHashMap<>();

    private FHIRPathFunctionRegistry() {
        registerFunctions();
    }

    public static FHIRPathFunctionRegistry getInstance() {
        return INSTANCE;
    }

    public void register(FHIRPathFunction function) {
        functionMap.put(function.getName(), function);
    }

    public FHIRPathFunction getFunction(String functionName) {
        return functionMap.get(functionName);
    }

    public Set<String> getFunctionNames() {
        return Collections.unmodifiableSet(functionMap.keySet());
    }

    private void registerFunctions() {
        register(new AllFalseFunction());
        register(new AllTrueFunction());
        register(new AnyFalseFunction());
        register(new AnyTrueFunction());
        register(new BetweenFunction());
        register(new CheckModifiersFunction());
        register(new ChildrenFunction());
        register(new CombineFunction());
        register(new ConformsToFunction());
        register(new ContainsFunction());
        register(new ConvertsToBooleanFunction());
        register(new ConvertsToDateFunction());
        register(new ConvertsToDateTimeFunction());
        register(new ConvertsToDecimalFunction());
        register(new ConvertsToIntegerFunction());
        register(new ConvertsToQuantityFunction());
        register(new ConvertsToStringFunction());
        register(new ConvertsToTimeFunction());
        register(new CountFunction());
        register(new DescendantsFunction());
        register(new DistinctFunction());
        register(new EmptyFunction());
        register(new EndsWithFunction());
        register(new ExcludeFunction());
        register(new ExtensionFunction());
        register(new FirstFunction());
        register(new GetValueFunction());
        register(new HasValueFunction());
        register(new HtmlChecksFunction());
        register(new IndexOfFunction());
        register(new IntersectFunction());
        register(new IsDistinctFunction());
        register(new ItemFunction());
        register(new LastFunction());
        register(new LengthFunction());
        register(new LowerFunction());
        register(new MatchesFunction());
        register(new MemberOfFunction());
        register(new NotFunction());
        register(new NowFunction());
        register(new ReplaceFunction());
        register(new ReplaceMatchesFunction());
        register(new ResolveFunction());
        register(new SingleFunction());
        register(new SkipFunction());
        register(new SliceFunction());
        register(new StartsWithFunction());
        register(new SubsetOfFunction());
        register(new SubstringFunction());
        register(new SupersetOfFunction());
        register(new TailFunction());
        register(new TakeFunction());
        register(new TimeOfDayFunction());
        register(new ToBooleanFunction());
        register(new ToCharsFunction());
        register(new ToDateFunction());
        register(new ToDateTimeFunction());
        register(new ToDecimalFunction());
        register(new ToIntegerFunction());
        register(new ToQuantityFunction());
        register(new ToStringFunction());
        register(new ToTimeFunction());
        register(new TodayFunction());
        register(new TypeFunction());
        register(new UnionFunction());
        register(new UpperFunction());

        // register terminology functions
        register(new ExpandFunction());
        register(new LookupFunction());
        register(new SubsumedByFunction());
        register(new SubsumesFunction());
        register(new TranslateFunction());
        register(new ValidateCSFunction());
        register(new ValidateVSFunction());
    }
}