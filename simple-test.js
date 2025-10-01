// 날짜 필드 테스트 스크립트
console.log('=== 날짜 필드 동작 테스트 시작 ===');

// 1. 날짜 필드 존재 확인
function checkDateFields() {
    console.log('\n1. 날짜 필드 존재 확인:');
    const startDateField = document.querySelector('input[data-field="contractStartDate"]');
    const endDateField = document.querySelector('input[data-field="contractEndDate"]');

    console.log('- 계약시작일 필드:', startDateField);
    console.log('- 계약종료일 필드:', endDateField);

    if (startDateField) {
        console.log('  * 시작일 필드 타입:', startDateField.type);
        console.log('  * 시작일 필드 값:', startDateField.value);
    }

    if (endDateField) {
        console.log('  * 종료일 필드 타입:', endDateField.type);
        console.log('  * 종료일 필드 값:', endDateField.value);
    }

    return { startDateField, endDateField };
}

// 2. 이벤트 리스너 테스트
function testEventListeners() {
    console.log('\n2. 이벤트 리스너 테스트:');
    const { startDateField, endDateField } = checkDateFields();

    if (startDateField) {
        // 테스트 값 설정
        startDateField.value = '2024-01-01';
        console.log('- 시작일에 테스트 값 설정:', startDateField.value);

        // input 이벤트 수동 트리거
        startDateField.dispatchEvent(new Event('input', { bubbles: true }));
        startDateField.dispatchEvent(new Event('change', { bubbles: true }));
        console.log('- 시작일 이벤트 트리거 완료');
    }

    if (endDateField) {
        // 테스트 값 설정
        endDateField.value = '2024-12-31';
        console.log('- 종료일에 테스트 값 설정:', endDateField.value);

        // input 이벤트 수동 트리거
        endDateField.dispatchEvent(new Event('input', { bubbles: true }));
        endDateField.dispatchEvent(new Event('change', { bubbles: true }));
        console.log('- 종료일 이벤트 트리거 완료');
    }
}

// 3. 미리보기 업데이트 확인
function checkPreviewUpdate() {
    console.log('\n3. 미리보기 업데이트 확인:');
    const preview = document.getElementById('previewSurface');
    if (preview) {
        console.log('- 미리보기 내용 (일부):', preview.innerHTML.substring(0, 200));

        // CONTRACT_START_DATE와 CONTRACT_END_DATE 검색
        const hasStartDate = preview.innerHTML.includes('2024년 01월 01일');
        const hasEndDate = preview.innerHTML.includes('2024년 12월 31일');

        console.log('- 시작일 반영됨:', hasStartDate);
        console.log('- 종료일 반영됨:', hasEndDate);

        if (!hasStartDate || !hasEndDate) {
            console.log('- 미리보기에서 [CONTRACT_START_DATE] 검색:', preview.innerHTML.includes('[CONTRACT_START_DATE]'));
            console.log('- 미리보기에서 [CONTRACT_END_DATE] 검색:', preview.innerHTML.includes('[CONTRACT_END_DATE]'));
        }
    }
}

// 4. 프리셋 폼 데이터 확인
function checkPresetFormData() {
    console.log('\n4. 프리셋 폼 데이터 확인:');
    if (typeof presetFormData !== 'undefined') {
        console.log('- presetFormData 존재:', true);
        console.log('- contractStartDate 값:', presetFormData.contractStartDate);
        console.log('- contractEndDate 값:', presetFormData.contractEndDate);
        console.log('- 전체 데이터:', presetFormData);
    } else {
        console.log('- presetFormData 존재:', false);
    }
}

// 전체 테스트 실행
function runAllTests() {
    setTimeout(() => {
        checkDateFields();
        testEventListeners();

        // 미리보기 업데이트를 위해 약간의 지연 후 확인
        setTimeout(() => {
            checkPreviewUpdate();
            checkPresetFormData();
            console.log('\n=== 테스트 완료 ===');
        }, 1000);
    }, 2000); // 2초 후 실행 (페이지 로딩 완료 대기)
}

// 페이지 로드 후 자동 실행
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', runAllTests);
} else {
    runAllTests();
}

// 수동 실행 함수도 제공
window.testDateFields = runAllTests;

console.log('테스트 스크립트 로드 완료. runAllTests() 또는 testDateFields()를 호출하여 테스트를 실행하세요.');