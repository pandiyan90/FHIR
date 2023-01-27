/*
 * (C) Copyright IBM Corp. 2021, 2022
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package net.sovrinhealth.fhir.database.utils.query.expression;

import java.util.List;

import net.sovrinhealth.fhir.database.utils.query.FromClause;
import net.sovrinhealth.fhir.database.utils.query.FromItem;
import net.sovrinhealth.fhir.database.utils.query.GroupByClause;
import net.sovrinhealth.fhir.database.utils.query.HavingClause;
import net.sovrinhealth.fhir.database.utils.query.OrderByClause;
import net.sovrinhealth.fhir.database.utils.query.PaginationClause;
import net.sovrinhealth.fhir.database.utils.query.Select;
import net.sovrinhealth.fhir.database.utils.query.SelectList;
import net.sovrinhealth.fhir.database.utils.query.WhereClause;
import net.sovrinhealth.fhir.database.utils.query.With;
import net.sovrinhealth.fhir.database.utils.query.node.ExpNode;

/**
 * Defines the contract for rendering statements. Can be used to address
 * small differences in database syntax and some simple optimizations/query
 * rewrites if necessary
 */
public interface StatementRenderer<T> {

    /**
     * Render the select statement using each of the components, some of which
     * may be optional (null)
     * @param withClauses
     * @param distinct
     * @param selectList
     * @param fromClause
     * @param whereClause
     * @param groupByClause
     * @param havingClause
     * @param orderByClause
     * @param paginationClause
     * @param unionAll
     * @param union
     * @return
     */
    T select(List<With> withClauses, boolean distinct, SelectList selectList, FromClause fromClause, WhereClause whereClause, GroupByClause groupByClause, HavingClause havingClause,
        OrderByClause orderByClause, PaginationClause paginationClause, boolean unionAll, Select union);

    /**
     * Render a WITH foo AS (select ...) clause
     * @param subSelect
     * @param aliasValue
     * @return
     */
    T with (T subSelect, T aliasValue);

    /**
     * @param items
     * @return
     */
    T from(List<FromItem> items);

    /**
     * Render the given item
     * @param item
     * @return
     */
    T fromItem(FromItem item);

    /**
     * @param sub
     * @return
     */
    T rowSource(T sub);

    /**
     * @param subValue
     * @param aliasValue
     * @return
     */
    T fromItem(T subValue, T aliasValue);

    /**
     * @param alias
     * @return
     */
    T alias(String alias);

    /**
     * @param schemaName
     * @param tableName
     * @return
     */
    T rowSource(String schemaName, String tableName);

    /**
     * @param joinOnPredicate
     * @return
     */
    T render(ExpNode joinOnPredicate);

    /**
     * @param joinFromValue
     * @param joinOnValue
     * @return
     */
    T innerJoin(T joinFromValue, T joinOnValue);

    /**
     * @param joinFromValue
     * @param joinOnValue
     * @return
     */
    T leftOuterJoin(T joinFromValue, T joinOnValue);

    /**
     * @param joinFromValue
     * @param joinOnValue
     * @return
     */
    T fullOuterJoin(T joinFromValue, T joinOnValue);
}
