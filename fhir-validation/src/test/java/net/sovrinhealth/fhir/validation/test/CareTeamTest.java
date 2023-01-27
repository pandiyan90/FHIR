/*
 * (C) Copyright IBM Corp. 2019
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.validation.test;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;

import net.sovrinhealth.fhir.model.format.Format;
import net.sovrinhealth.fhir.model.parser.FHIRParser;
import net.sovrinhealth.fhir.model.resource.CareTeam;
import net.sovrinhealth.fhir.model.resource.OperationOutcome.Issue;
import net.sovrinhealth.fhir.path.FHIRPathNode;
import net.sovrinhealth.fhir.path.evaluator.FHIRPathEvaluator;
import net.sovrinhealth.fhir.path.evaluator.FHIRPathEvaluator.EvaluationContext;
import net.sovrinhealth.fhir.validation.FHIRValidator;

public class CareTeamTest {
    public static void main(String[] args) throws Exception {
        try (InputStream in = CareTeamTest.class.getClassLoader().getResourceAsStream("JSON/careteam.json")) {
            CareTeam careTeam = FHIRParser.parser(Format.JSON).parse(in);
            List<Issue> issues = FHIRValidator.validator().validate(careTeam);
            for (Issue issue : issues) {
                System.out.println("severity: " + issue.getSeverity().getValue() + ", details: " + issue.getDetails().getText().getValue() + ", expression: " + issue.getExpression().get(0).getValue());
            }

            FHIRPathEvaluator evaluator = FHIRPathEvaluator.evaluator();
            EvaluationContext evaluationContext = new EvaluationContext(careTeam);

            Collection<FHIRPathNode> result = evaluator.evaluate(evaluationContext, "CareTeam.participant");
            for (FHIRPathNode node : result) {
                result = evaluator.evaluate(evaluationContext, "member.resolve() is Practitioner", node);
                System.out.println("result: " + result);
            }
        }
    }
}
