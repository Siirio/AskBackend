param(
    [Parameter(Mandatory = $true)]
    [string]$BaseUrl,
    [string]$DatasetPath = (Join-Path $PSScriptRoot "dataset-v1.json"),
    [int]$PageSize = 10,
    [string]$OutputPath
)

$dataset = Get-Content -Raw -Encoding utf8 $DatasetPath | ConvertFrom-Json
$caseResults = @()

foreach ($case in $dataset.cases) {
    $request = @{
        raw_query = $case.raw_query
        mode = $case.mode
        page = 0
        page_size = $PageSize
        sort = "relevance"
        locale = $case.cohort
    }
    $request.explicit_filters = @{}
    if ($null -ne $case.max_price) {
        $request.explicit_filters.max_price = $case.max_price
    }
    if ($null -ne $case.city -and $case.city -ne "") {
        $request.explicit_filters.city = $case.city
    }

    $startedAt = [System.Diagnostics.Stopwatch]::StartNew()
    $response = Invoke-RestMethod -Uri "$($BaseUrl.TrimEnd('/'))/api/v1/search" -Method Post -ContentType "application/json" -Body ($request | ConvertTo-Json -Depth 5)
    $startedAt.Stop()
    $cards = @($response.sections | ForEach-Object { $_.cards } | Where-Object { $null -ne $_ })
    $topThree = @($cards | Select-Object -First 3)
    $topTen = @($cards | Select-Object -First 10)
    $relevantTitles = @($case.relevant_titles)
    $topThreeRelevant = @($topThree | Where-Object { $relevantTitles -contains $_.title }).Count
    $topTenRelevant = @($topTen | Where-Object { $relevantTitles -contains $_.title }).Count
    $firstRelevantRank = 0
    for ($index = 0; $index -lt $cards.Count; $index++) {
        if ($relevantTitles -contains $cards[$index].title) {
            $firstRelevantRank = $index + 1
            break
        }
    }
    $constraintViolations = @($response.sections | Where-Object { $_.kind -eq "EXACT" } | ForEach-Object { $_.cards } | Where-Object {
        ($null -ne $case.max_price -and $null -ne $_.price -and $_.price -gt $case.max_price)
    }).Count
    $categoryMismatches = @($cards | Where-Object { $_.component -ne $case.expected_component }).Count
    $cityViolations = @($cards | Where-Object {
        $null -ne $case.city -and $case.city -ne "" -and $_.branch_city -ne $case.city
    }).Count
    $zeroCorrect = if ($case.expected_zero) { $cards.Count -eq 0 } else { $true }
    $semanticMiss = -not $case.expected_zero -and $relevantTitles.Count -gt 0 -and $topTenRelevant -eq 0

    $caseResults += [pscustomobject]@{
        id = $case.id
        cohort = $case.cohort
        result_count = $cards.Count
        precision_at_3 = if ($topThree.Count -eq 0) { 0.0 } else { $topThreeRelevant / $topThree.Count }
        recall_at_10 = if ($relevantTitles.Count -eq 0) { 1.0 } else { $topTenRelevant / $relevantTitles.Count }
        reciprocal_rank = if ($firstRelevantRank -eq 0) { 0.0 } else { 1.0 / $firstRelevantRank }
        zero_result_correct = $zeroCorrect
        explicit_constraint_violations = $constraintViolations
        category_mismatches = $categoryMismatches
        city_filter_violations = $cityViolations
        semantic_miss = $semanticMiss
        expected_zero = [bool]$case.expected_zero
        latency_ms = $startedAt.ElapsedMilliseconds
    }
}

$cohorts = @($caseResults | Group-Object cohort | ForEach-Object {
    [pscustomobject]@{
        cohort = $_.Name
        case_count = $_.Count
        precision_at_3 = ($_.Group | Measure-Object precision_at_3 -Average).Average
        recall_at_10 = ($_.Group | Measure-Object recall_at_10 -Average).Average
        mrr = ($_.Group | Measure-Object reciprocal_rank -Average).Average
    }
})

$report = [pscustomobject]@{
    dataset_version = $dataset.version
    generated_at = [DateTimeOffset]::UtcNow.ToString("O")
    case_count = $caseResults.Count
    precision_at_3 = ($caseResults | Measure-Object precision_at_3 -Average).Average
    recall_at_10 = ($caseResults | Measure-Object recall_at_10 -Average).Average
    mrr = ($caseResults | Measure-Object reciprocal_rank -Average).Average
    zero_result_correctness = (@($caseResults | Where-Object zero_result_correct).Count / $caseResults.Count)
    explicit_constraint_violations = ($caseResults | Measure-Object explicit_constraint_violations -Sum).Sum
    category_mismatches = ($caseResults | Measure-Object category_mismatches -Sum).Sum
    wrong_category_rate = (($caseResults | Measure-Object category_mismatches -Sum).Sum / [Math]::Max(1, ($caseResults | Measure-Object result_count -Sum).Sum))
    city_filter_violation_rate = (($caseResults | Measure-Object city_filter_violations -Sum).Sum / [Math]::Max(1, ($caseResults | Measure-Object result_count -Sum).Sum))
    semantic_miss_rate = (@($caseResults | Where-Object semantic_miss).Count / [Math]::Max(1, @($caseResults | Where-Object { -not $_.expected_zero }).Count))
    cohorts = $cohorts
    cases = $caseResults
}

$json = $report | ConvertTo-Json -Depth 8
if ($OutputPath) {
    Set-Content -Path $OutputPath -Value $json -Encoding utf8
}
$json
