// 날짜 필드 디버깅을 위한 추가 스크립트
console.log('=== 날짜 필드 직접 확인 스크립트 ===');

// 수동으로 필드 값 확인
function checkDateFieldsDirectly() {
    console.log('\n[MANUAL CHECK] 현재 날짜 필드들의 실제 값:');

    const startDateField = document.querySelector('input[data-field="contractStartDate"]');
    const endDateField = document.querySelector('input[data-field="contractEndDate"]');

    if (startDateField) {
        console.log('- 시작일 필드 존재:', true);
        console.log('  * ID:', startDateField.id);
        console.log('  * Name:', startDateField.name);
        console.log('  * Type:', startDateField.type);
        console.log('  * Value:', '"' + startDateField.value + '"');
        console.log('  * Value 길이:', startDateField.value.length);
        console.log('  * 부모 요소:', startDateField.closest('.preset-form-fields') ? '프리셋 폼 내부' : '프리셋 폼 외부');

        // 이벤트 리스너 테스트
        console.log('  * 테스트 값 설정...');
        startDateField.value = '2024-03-15';
        console.log('  * 테스트 후 값:', '"' + startDateField.value + '"');
    } else {
        console.log('- 시작일 필드 존재:', false);
    }

    if (endDateField) {
        console.log('- 종료일 필드 존재:', true);
        console.log('  * ID:', endDateField.id);
        console.log('  * Name:', endDateField.name);
        console.log('  * Type:', endDateField.type);
        console.log('  * Value:', '"' + endDateField.value + '"');
        console.log('  * Value 길이:', endDateField.value.length);
        console.log('  * 부모 요소:', endDateField.closest('.preset-form-fields') ? '프리셋 폼 내부' : '프리셋 폼 외부');

        // 이벤트 리스너 테스트
        console.log('  * 테스트 값 설정...');
        endDateField.value = '2024-12-31';
        console.log('  * 테스트 후 값:', '"' + endDateField.value + '"');
    } else {
        console.log('- 종료일 필드 존재:', false);
    }

    // 전체 프리셋 폼 필드 확인
    const allPresetFields = document.querySelectorAll('.preset-form-fields input, .preset-form-fields select, .preset-form-fields textarea');
    console.log('\n- 전체 프리셋 필드 개수:', allPresetFields.length);

    allPresetFields.forEach((field, index) => {
        if (field.dataset.field && field.dataset.field.includes('Date')) {
            console.log(`  [${index}] ${field.dataset.field}: "${field.value}" (타입: ${field.type})`);
        }
    });
}

// 이벤트 시뮬레이션
function simulateDateInput() {
    console.log('\n[EVENT SIMULATION] 날짜 입력 이벤트 시뮬레이션...');

    const startDateField = document.querySelector('input[data-field="contractStartDate"]');
    if (startDateField) {
        console.log('- 시작일 필드에 값 설정 및 이벤트 발생...');
        startDateField.value = '2024-06-01';

        // 다양한 이벤트 트리거
        startDateField.dispatchEvent(new Event('input', { bubbles: true }));
        startDateField.dispatchEvent(new Event('change', { bubbles: true }));

        console.log('- 이벤트 발생 후 값:', '"' + startDateField.value + '"');
    }

    const endDateField = document.querySelector('input[data-field="contractEndDate"]');
    if (endDateField) {
        console.log('- 종료일 필드에 값 설정 및 이벤트 발생...');
        endDateField.value = '2025-06-01';

        // 다양한 이벤트 트리거
        endDateField.dispatchEvent(new Event('input', { bubbles: true }));
        endDateField.dispatchEvent(new Event('change', { bubbles: true }));

        console.log('- 이벤트 발생 후 값:', '"' + endDateField.value + '"');
    }
}

// 프리셋 폼 데이터 확인
function checkPresetFormDataState() {
    console.log('\n[PRESET DATA] 프리셋 폼 데이터 상태:');

    if (typeof presetFormData !== 'undefined') {
        console.log('- presetFormData 존재:', true);
        console.log('- contractStartDate 값:', '"' + (presetFormData.contractStartDate || '') + '"');
        console.log('- contractEndDate 값:', '"' + (presetFormData.contractEndDate || '') + '"');
        console.log('- 전체 데이터 키들:', Object.keys(presetFormData));

        // 날짜 필드만 필터링
        const dateFields = {};
        Object.keys(presetFormData).forEach(key => {
            if (key.includes('Date')) {
                dateFields[key] = presetFormData[key];
            }
        });
        console.log('- 날짜 관련 필드들:', dateFields);
    } else {
        console.log('- presetFormData 존재:', false);
    }
}

// 전체 디버깅 실행
function runFullDebug() {
    setTimeout(() => {
        checkDateFieldsDirectly();
        checkPresetFormDataState();

        // 1초 후 이벤트 시뮬레이션
        setTimeout(() => {
            simulateDateInput();

            // 이벤트 시뮬레이션 후 데이터 다시 확인
            setTimeout(() => {
                console.log('\n=== 이벤트 시뮬레이션 후 상태 ===');
                checkPresetFormDataState();
            }, 1000);
        }, 1000);
    }, 2000); // 페이지 로딩 완료 대기
}

// 자동 실행
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', runFullDebug);
} else {
    runFullDebug();
}

// 수동 실행용
window.debugDateFields = runFullDebug;
window.checkDates = checkDateFieldsDirectly;
window.simulateDate = simulateDateInput;

console.log('날짜 필드 디버깅 스크립트 로드 완료');
console.log('- runFullDebug() 또는 debugDateFields(): 전체 디버깅 실행');
console.log('- checkDates() 또는 checkDateFieldsDirectly(): 현재 상태만 확인');
console.log('- simulateDate() 또는 simulateDateInput(): 이벤트 시뮬레이션');