package com.kgignatyev.fss.service.common.data

import com.kgignatyev.fss_svc.api.fsssvc.v1.model.V1ListSummary
import jakarta.persistence.criteria.Predicate
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.JpaSpecificationExecutor

data class SearchSummary(val offset: Long = 0, val count:Int = 0, val total:Long = 0) {
    fun toApiListSummary():  V1ListSummary {
        val r = V1ListSummary()
        r.offset = offset
        r.total = total
        r.size = count
        return r
    }
}
data class SearchResult<T>(val items:List<T>, val summary: SearchSummary)



interface SearchableRepo<T> {

    fun search(searchExpr:String, sortExpr:String, offset:Long = 0, limit:Int = 20): SearchResult<T>

    fun searchImpl(searchExpr: String, sortExpr:String, offset: Long, limit: Int, repo:JpaSpecificationExecutor<T>): SearchResult<T> {
        var parts = searchExpr.split(" ").map { it.trim() }
        val spec = if (parts.size >= 3) {
            Specification.where<T> { root, query, builder ->
                val predicates = mutableListOf<Predicate>()
                while (parts.size >= 3) {
                    val (f, op, criteria) = parts.take(3)
                    val noQuotesCriteria = criteria.replace("'","")
                        .replace("\"","")
                    parts = parts.drop(3)
                    val getF = root.get<String>(f)
                    when (op) {
                        "=" -> {
                            predicates.add(builder.equal(getF, noQuotesCriteria))
                        }
                        ">" -> {
                            predicates.add(builder.greaterThan(getF, noQuotesCriteria))
                        }
                        "<" -> {
                            predicates.add(builder.lessThan(getF, noQuotesCriteria))
                        }
                        ">=" -> {
                            predicates.add(builder.greaterThanOrEqualTo(getF, noQuotesCriteria))
                        }
                        "<=" -> {
                            predicates.add(builder.lessThanOrEqualTo(getF, noQuotesCriteria))
                        }
                        "!=" -> {
                            predicates.add(builder.notEqual(getF, noQuotesCriteria))
                        }
                        "like" -> {
                            predicates.add(builder.like(getF, noQuotesCriteria))
                        }
                        "ilike" -> {
                            predicates.add(builder.like(builder.lower(getF), noQuotesCriteria.lowercase()))
                        }
                        else -> {
                            throw IllegalArgumentException("Unknown operator: $op")
                        }
                    }

                    if (parts.isNotEmpty()) {
                        val op = parts.take(1)
                        parts = parts.drop(1)
                    }
                }
                builder.or( *predicates.toTypedArray())
            }
        } else {
            Specification.unrestricted<T>()
        }
        val pageN = (offset / limit).toInt()
        val page: Pageable = pageableOf(pageN, limit, sortExpr)
        val itemsPage: Page<T> = repo.findAll(spec, page)
        val total = itemsPage.totalElements
        val summary = SearchSummary(offset, itemsPage.size, total)
        val r = SearchResult<T>(itemsPage.toList(), summary)
        return r
    }

    fun pageableOf(pageN: Int, limit: Int, sortExpr: String): Pageable {
        var pageRequest = PageRequest.of(pageN, limit)
        sortExpr.split(",")
            .map { it.trim() }.forEach { sort ->
              val parts = sort.split(" ")
                if (parts.size == 2) {
                    val ( field, dir) = parts
                    pageRequest = pageRequest.withSort(Sort.Direction.fromString(dir.uppercase()), field)
                }else{
                    pageRequest = pageRequest.withSort(Sort.Direction.ASC, sort)
                }
            }
        return pageRequest
    }
}
